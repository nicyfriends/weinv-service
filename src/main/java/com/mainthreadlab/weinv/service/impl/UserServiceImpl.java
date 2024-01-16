package com.mainthreadlab.weinv.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainthreadlab.weinv.commons.Constants;
import com.mainthreadlab.weinv.commons.Utils;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.dto.request.CredentialsRequest;
import com.mainthreadlab.weinv.dto.request.UpdateUserRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.response.LoginResponse;
import com.mainthreadlab.weinv.dto.response.UserResponse;
import com.mainthreadlab.weinv.dto.security.AuthUpdateUserRequest;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.exception.ForbiddenException;
import com.mainthreadlab.weinv.exception.ResourceNotFoundException;
import com.mainthreadlab.weinv.exception.UnauthorizedException;
import com.mainthreadlab.weinv.exception.UniqueConstraintViolationException;
import com.mainthreadlab.weinv.mapper.UserMapper;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.WeddingGuest;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.enums.Role;
import com.mainthreadlab.weinv.repository.UserRepository;
import com.mainthreadlab.weinv.repository.WeddingGuestRepository;
import com.mainthreadlab.weinv.repository.WeddingRepository;
import com.mainthreadlab.weinv.service.EmailService;
import com.mainthreadlab.weinv.service.UserService;
import com.mainthreadlab.weinv.service.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.mainthreadlab.weinv.commons.Utils.isSourceDateBeforeTargetDate;
import static com.mainthreadlab.weinv.model.enums.ErrorKey.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final WeddingRepository weddingRepository;
    private final WeddingGuestRepository weddingGuestRepository;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final EmailService emailSender;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${weinv.auth-server.login}")
    private String uriToken;

    @Value("${weinv.client-id}")
    private String clientId;

    @Value("${weinv.client-secret}")
    private String clientSecret;

    @Value("${weinv.mail.account.wedding-responsible.body.fr}")
    private String frAccountCreationBodyFilePath;

    @Value("${weinv.mail.account.wedding-responsible.body.en}")
    private String enAccountCreationBodyFilePath;

    @Value("${weinv.mail.account.wedding-responsible.subject}")
    private String accountCreationSubject;


    @Override
    @Transactional
    public LoginResponse login(CredentialsRequest credentialsRequest) throws URISyntaxException, IOException {
        log.info("[login] - start: username={}", credentialsRequest.getUsername());

        // in order to simplify user usage
        credentialsRequest.setUsername(credentialsRequest.getUsername().trim().toLowerCase());
        credentialsRequest.setPassword(credentialsRequest.getPassword().trim().toLowerCase());

        User user = userRepository.findByUsernameAndEnabledTrue(credentialsRequest.getUsername());
        if (user == null) {
            log.error("[login] - user not found, username = {}", credentialsRequest.getUsername());
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        Wedding wedding = weddingRepository.findByResponsible(user);
        WeddingGuest weddingGuest = weddingGuestRepository.findByGuest(user);
        if (wedding == null && weddingGuest != null) {
            wedding = weddingGuest.getWedding();
        }

        if (wedding != null && isSourceDateBeforeTargetDate(wedding.getDate(), new Date())) {
            log.error("[login] - wedding date is expired, date={}", wedding.getDate());
            throw new ForbiddenException();
        }

        URI uri = new URIBuilder(uriToken)
                .addParameter("grant_type", "password")
                .addParameter("username", credentialsRequest.getUsername())
                .addParameter("password", credentialsRequest.getPassword())
                .build();

        HttpPost request = new HttpPost();
        request.setURI(uri);
        request.setHeader(Constants.AUTHORIZATION, Utils.getBasicAuthenticationHeader(clientId, clientSecret));

        log.info("[login] - trying authentication in authorization-server: username={}", credentialsRequest.getUsername());

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse httpResponse = httpClient.execute(request)) {

            String bodyString = Utils.getResponseBody(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                String errorDescription = Constants.ERROR_DESCRIPTION.formatted(httpResponse.getStatusLine().getStatusCode(), bodyString);
                log.error("[login] - wrong password: {}", errorDescription);
                throw new UnauthorizedException(WRONG_PASSWORD);
            }

            LoginResponse loginResponse = objectMapper.readValue(bodyString, LoginResponse.class);

            String uuidWedding = null;
            if (StringUtils.isNotBlank(user.getRoles())) {
                List<String> roles = Arrays.asList(user.getRoles().split(","));
                if (roles.contains(Role.USER.getDescription())) {
                    wedding = weddingRepository.findByResponsible(user);
                    if (wedding != null) {
                        uuidWedding = wedding.getUuid();
                    }
                } else if (roles.contains(Role.GUEST.getDescription()) && weddingGuest != null) {
                    uuidWedding = weddingGuest.getWedding().getUuid();
                }
            }

            loginResponse.setUuidWedding(uuidWedding);
            loginResponse.setUuidUser(user.getUuid());
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setEventType(user.getEventType());
            List<String> roles = Arrays.stream(user.getRoles().split(",")).map(userMapper::mapRole).toList();
            loginResponse.setRoles(roles);
            user.setLastLoginDate(new Date());

            log.info("[login] - success: username={}", credentialsRequest.getUsername());
            return loginResponse;
        }
    }


    /**
     * Register a responsible for an event (only 'admin' can do that).
     */
    @Override
    @Transactional
    public String registerWeddingResponsible(JwtDetails jwtDetails, UserRequest userRequest) {
        log.info("[registerWeddingResponsible] - start: username={}", userRequest.getUsername());

        // in order to simplify user usage
        userRequest.setUsername(userRequest.getUsername().trim().toLowerCase());
        userRequest.setPassword(userRequest.getPassword().trim().toLowerCase());
        userRequest.setFirstName(StringUtils.capitalize(userRequest.getFirstName()));
        userRequest.setLastName(StringUtils.capitalize(userRequest.getLastName()));

        log.info("[registerWeddingResponsible] - save in weinv");
        userRequest.setRole(Role.USER);
        if (userRequest.getPrice() == null) userRequest.setPrice(0D);
        User user = save(userRequest);

        log.info("[registerWeddingResponsible] - save in authorization-server");
        AuthUserRequest authUserRequest = userMapper.toAuthUser(userRequest);
        customUserDetailsService.addUserDetails(authUserRequest);

        String template = Utils.readFileFromResource(enAccountCreationBodyFilePath);
        if (Language.FR.equals(user.getLanguage())) {
            template = Utils.readFileFromResource(frAccountCreationBodyFilePath);
        }
        template = template.replace("{username}", userRequest.getUsername()).replace("{password}", userRequest.getPassword());

        emailSender.sendHtmlEmail(user.getEmail(), accountCreationSubject, template);
        emailSender.sendHtmlEmail(jwtDetails.getEmail(), accountCreationSubject, template);

        log.info("[registerWeddingResponsible] - success: username={}", userRequest.getUsername());
        return user.getUuid();
    }

    @Override
    @Transactional
    public void deleteUser(String uuidUser, String uuidWedding) {
        log.info("[deleteUser] - start: uuidUser={}, uuidWedding={}", uuidUser, uuidWedding);

        User user = getByUuid(uuidUser);
        if (user == null) {
            log.error("[deleteUser] - User not found, uuid={}", uuidUser);
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        log.info("[deleteUser] - delete wedding invitation (if it exists)");
        if (StringUtils.isNotBlank(uuidWedding)) {
            Wedding wedding = weddingRepository.findByUuid(uuidWedding);
            if (wedding != null) {
                weddingGuestRepository.deleteByWeddingAndGuest(wedding, user);
            }
        }

        log.info("[deleteUser] - delete user from weinv");
        userRepository.delete(user);

        log.info("[deleteUser] - delete user from authorization-server");
        customUserDetailsService.delete(user.getUsername());

        log.info("[deleteUser] - success: username={}", user.getUsername());
    }

    @Override
    public User save(UserRequest userRequest) {
        User user = userRepository.findByUsername(userRequest.getUsername());
        if (user != null) {
            log.error("[save] - User already exists, uuid={}", userRequest.getUsername());
            throw new UniqueConstraintViolationException(USER_ALREADY_EXISTS);
        }
        if (StringUtils.isBlank(userRequest.getPhoneNumber())) {
            userRequest.setPhoneNumber("+243");  // default country code
        }
        userRequest.setUsername(userRequest.getUsername().trim().toLowerCase());
        userRequest.setPassword(userRequest.getPassword().trim().toLowerCase());
        return userRepository.save(userMapper.toEntity(userRequest));
    }

    @Override
    @Transactional
    public UserResponse getUser(String uuid) {
        log.info("[GetUser] - start: uuid={}", uuid);

        UserResponse userResponse = null;
        User user = getByUuid(uuid);
        if (user != null) {
            userResponse = userMapper.toModel(user);
            WeddingGuest weddingGuest = weddingGuestRepository.findByGuest(user);
            if (weddingGuest != null) {
                userResponse.setTableNumber(weddingGuest.getTableNumber());
            }
        }

        log.info("[GetUser] - success: {}", userResponse != null ? userResponse.toString() : null);
        log.info("[GetUser] - end");
        return userResponse;

    }

    @Override
    public User getByUuid(String uuid) {
        return userRepository.findByUuidAndEnabledTrue(uuid);
    }

    @Override
    @Transactional
    public void updateUser(String uuid, String uuidWedding, UpdateUserRequest updateUserRequest) {

        String uuidFinal = StringUtils.isNotBlank(updateUserRequest.getUuid()) ? updateUserRequest.getUuid() : uuid;
        log.info("[UpdateUser] - start: uuid={}", uuidFinal);

        updateUserRequest.setFirstName(StringUtils.capitalize(updateUserRequest.getFirstName()));
        updateUserRequest.setLastName(StringUtils.capitalize(updateUserRequest.getLastName()));

        User user = getByUuid(uuidFinal);
        if (user == null) {
            log.error("[UpdateUser] - User not found, uuid={}", uuidFinal);
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        // update in weinv
        if (Objects.nonNull(updateUserRequest.getTableNumber())) {
            Wedding wedding = weddingRepository.findByUuid(uuidWedding);
            if (wedding != null) {
                WeddingGuest weddingGuest = weddingGuestRepository.findByWeddingAndGuest(wedding, user);
                if (weddingGuest != null) {
                    weddingGuest.setTableNumber(updateUserRequest.getTableNumber());
                }
            }
        }
        updateUserFromAuthzServer(updateUserRequest, user);

        log.info("[UpdateUser] - success");
        log.info("[UpdateUser] - end");

    }

    private void updateUserFromAuthzServer(UpdateUserRequest updateUserRequest, User user) {
        AuthUpdateUserRequest authUpdateUserRequest = new AuthUpdateUserRequest();
        authUpdateUserRequest.setUsername(user.getUsername());
        authUpdateUserRequest.setCurrentPassword(updateUserRequest.getCurrentPassword());
        authUpdateUserRequest.setNewPassword(updateUserRequest.getNewPassword());

        if (StringUtils.isNotBlank(updateUserRequest.getEmail())) {
            user.setEmail(updateUserRequest.getEmail());
            authUpdateUserRequest.setEmail(updateUserRequest.getEmail());
        }
        if (StringUtils.isNotBlank(updateUserRequest.getFirstName())) {
            user.setFirstName(updateUserRequest.getFirstName());
        }
        if (StringUtils.isNotBlank(updateUserRequest.getLastName())) {
            user.setLastName(updateUserRequest.getLastName());
        }
        if (StringUtils.isNotBlank(updateUserRequest.getPhoneNumber())) {
            user.setPhoneNumber(updateUserRequest.getPhoneNumber());
        }
        if (updateUserRequest.getLanguage() != null) {
            user.setLanguage(updateUserRequest.getLanguage());
        }

        log.info("[UpdateUser] - (weinv > authorization-server)");
        customUserDetailsService.updateUser(authUpdateUserRequest);
    }

}
