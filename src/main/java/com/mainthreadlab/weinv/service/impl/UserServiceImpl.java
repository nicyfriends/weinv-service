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
import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.enums.Role;
import com.mainthreadlab.weinv.repository.InvitationRepository;
import com.mainthreadlab.weinv.repository.UserRepository;
import com.mainthreadlab.weinv.repository.EventRepository;
import com.mainthreadlab.weinv.service.EmailService;
import com.mainthreadlab.weinv.service.UserService;
import com.mainthreadlab.weinv.service.EventService;
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

    private final EventRepository eventRepository;
    private final InvitationRepository invitationRepository;
    private final ObjectMapper objectMapper;
    private final UserMapper mapper;
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

        Event event = eventRepository.findByResponsible(user);
        Invitation invitation = invitationRepository.findByGuest(user);
        if (event == null && invitation != null) {
            event = invitation.getEvent();
        }

        if (event != null && isSourceDateBeforeTargetDate(event.getDate(), new Date())) {
            log.error("[login] - wedding date is expired, date={}", event.getDate());
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
                    event = eventRepository.findByResponsible(user);
                    if (event != null) {
                        uuidWedding = event.getUuid();
                    }
                } else if (roles.contains(Role.GUEST.getDescription()) && invitation != null) {
                    uuidWedding = invitation.getEvent().getUuid();
                }
            }

            loginResponse.setUuidWedding(uuidWedding);
            loginResponse.setUuidUser(user.getUuid());
            loginResponse.setFirstName(user.getFirstName());
            loginResponse.setLastName(user.getLastName());
            loginResponse.setEventType(user.getEventType());
            List<String> roles = Arrays.stream(user.getRoles().split(",")).map(mapper::mapRole).toList();
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
        log.info("[register wedding responsible] - start: username={}", userRequest.getUsername());

        // in order to simplify user usage
        userRequest.setUsername(Utils.toLowerCase(userRequest.getUsername()));
        userRequest.setPassword(Utils.toLowerCase(userRequest.getPassword()));
        userRequest.setFirstName(StringUtils.capitalize(userRequest.getFirstName()));
        userRequest.setLastName(StringUtils.capitalize(userRequest.getLastName()));

        log.info("[register wedding responsible] - save in weinv");
        userRequest.setRole(Role.USER);
        if (userRequest.getPrice() == null) userRequest.setPrice(0D);
        User user = save(userRequest);

        log.info("[register wedding responsible] - save in authorization-server");
        AuthUserRequest authUserRequest = mapper.toAuthUser(userRequest);
        customUserDetailsService.addUserDetails(authUserRequest);

        String template = Utils.readFileFromResource(frAccountCreationBodyFilePath)
                .replace("{username}", userRequest.getUsername())
                .replace("{password}", userRequest.getPassword());

        emailSender.sendHtmlEmail(user.getEmail(), accountCreationSubject, template);
        emailSender.sendHtmlEmail(jwtDetails.getEmail(), accountCreationSubject, template);

        log.info("[register wedding responsible] - end");
        return user.getUuid();
    }

    @Override
    @Transactional
    public void deleteGuestInvitation(String uuidUser, String uuidWedding) {
        log.info("[delete guest invitation] - start: uuidUser={}, uuidWedding={}", uuidUser, uuidWedding);

        User user = getByUuid(uuidUser);
        if (user == null) {
            log.error("[delete guest invitation] - user not found, uuid={}", uuidUser);
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        log.info("[delete guest invitation] - delete wedding invitation (if it exists)");
        if (StringUtils.isNotBlank(uuidWedding)) {
            Event event = eventRepository.findByUuid(uuidWedding);
            if (event != null) {
                Invitation invitation = invitationRepository.findByWeddingAndGuest(event, user);
                invitationRepository.delete(invitation);
                EventService.updateStatusInvitationNumber(event, invitation.getStatus(), invitation.getTotalInvitations(), "-");
            }
        }

        log.info("[delete guest invitation] - delete user from weinv");
        userRepository.delete(user);

        log.info("[delete guest invitation] - delete user from authorization-server");
        customUserDetailsService.delete(user.getUsername());

        log.info("[delete guest invitation] - end");
    }

    @Override
    public User save(UserRequest userRequest) {
        User user = userRepository.findByUsername(userRequest.getUsername());
        if (user != null) {
            log.error("[save] - user already exists, uuid={}", userRequest.getUsername());
            throw new UniqueConstraintViolationException(USER_ALREADY_EXISTS);
        }
        if (StringUtils.isBlank(userRequest.getPhoneNumber())) {
            userRequest.setPhoneNumber("+243");  // default country code
        }
        userRequest.setUsername(Utils.toLowerCase(userRequest.getUsername()));
        userRequest.setPassword(Utils.toLowerCase(userRequest.getPassword()));
        return userRepository.save(mapper.toEntity(userRequest));
    }

    @Override
    @Transactional
    public UserResponse getUser(String uuid) {
        log.info("[get user] - start: uuid={}", uuid);

        UserResponse userResponse = null;
        User user = getByUuid(uuid);
        if (user != null) {
            userResponse = mapper.toModel(user);
            Invitation invitation = invitationRepository.findByGuest(user);
            if (invitation != null) {
                userResponse.setTableNumber(invitation.getTableNumber());
            }
        }

        log.info("[get user] - end");
        return userResponse;

    }

    @Override
    public User getByUuid(String uuid) {
        return userRepository.findByUuidAndEnabledTrue(uuid);
    }

    @Override
    @Transactional
    public void updateUser(String uuid, String uuidWedding, UpdateUserRequest request) {

        String uuidFinal = StringUtils.isNotBlank(request.getUuid()) ? request.getUuid() : uuid;
        log.info("[update user] - start: uuid={}", uuidFinal);

        User user = getByUuid(uuidFinal);
        if (user == null) {
            log.error("[update user] - user not found, uuid={}", uuidFinal);
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        // update in weinv
        if (Objects.nonNull(request.getTableNumber())) {
            Event event = eventRepository.findByUuid(uuidWedding);
            if (event != null) {
                Invitation invitation = invitationRepository.findByWeddingAndGuest(event, user);
                if (invitation != null) {
                    invitation.setTableNumber(request.getTableNumber());
                    if (request.getTotalInvitations() != null) {
                        EventService.updateStatusInvitationNumber(event, invitation.getStatus(), invitation.getTotalInvitations(), "-");
                        invitation.setTotalInvitations(request.getTotalInvitations());
                        EventService.updateStatusInvitationNumber(event, invitation.getStatus(), invitation.getTotalInvitations(), "+");
                    }
                }
            }
        }
        processUpdate(request, user);

        log.info("[update user] - end");
    }

    private void processUpdate(UpdateUserRequest request, User user) {
        if (StringUtils.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getFirstName())) {
            user.setFirstName(StringUtils.capitalize(request.getFirstName()));
        }
        if (StringUtils.isNotBlank(request.getLastName())) {
            user.setLastName(StringUtils.capitalize(request.getLastName()));
        }
        if (StringUtils.isNotBlank(request.getHusband())) {
            user.setHusband(StringUtils.capitalize(request.getHusband()));
        }
        if (StringUtils.isNotBlank(request.getWife())) {
            user.setWife(StringUtils.capitalize(request.getWife()));
        }
        if (StringUtils.isNotBlank(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }
        user.setCouple(request.isCouple());

        AuthUpdateUserRequest authUpdateUserRequest = mapper.map(request);
        authUpdateUserRequest.setUsername(user.getUsername());
        log.info("[update user] - (weinv > authorization-server)");
        customUserDetailsService.updateUser(authUpdateUserRequest);
    }

}
