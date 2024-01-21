package com.mainthreadlab.weinv.service.security;

import com.mainthreadlab.weinv.dto.security.AuthUpdateUserRequest;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.dto.security.CustomUserDetails;
import com.mainthreadlab.weinv.exception.BadCredentialsException;
import com.mainthreadlab.weinv.exception.BadRequestException;
import com.mainthreadlab.weinv.exception.ResourceNotFoundException;
import com.mainthreadlab.weinv.exception.UsernameNotFoundException;
import com.mainthreadlab.weinv.mapper.security.UserAuthMappers;
import com.mainthreadlab.weinv.model.security.Authority;
import com.mainthreadlab.weinv.model.security.UserAuth;
import com.mainthreadlab.weinv.repository.security.AuthorityRepository;
import com.mainthreadlab.weinv.repository.security.CustomUserDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mainthreadlab.weinv.model.enums.ErrorKey.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomUserDetailsService extends JdbcDaoImpl {

    private final CustomUserDetailsRepository customUserDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAuthMappers mapper;
    private final AuthorityRepository authorityRepository;


    @Autowired
    public void setDatasource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("AUTHORIZATION-SERVER > [loadUserByUsername] - start");
        UserAuth user = customUserDetailsRepository.findByUsername(username);
        if (user == null) {
            log.error("AUTHORIZATION-SERVER > [loadUserByUsername] - wrong username {}", username);
            throw new UsernameNotFoundException(WRONG_USERNAME);
        }
        CustomUserDetails userDetails = new CustomUserDetails(user, loadUserAuthorities(username));
        AccountStatusUserDetailsChecker accountStatusChecker = new AccountStatusUserDetailsChecker();
        accountStatusChecker.check(userDetails);

        logger.info("AUTHORIZATION-SERVER > [loadUserByUsername] - success");
        return userDetails;
    }

    @Override
    protected List<GrantedAuthority> loadUserAuthorities(String username) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        UserAuth user = customUserDetailsRepository.findByUsername(username);
        if (user != null) {
            user.getAuthorities().forEach(a -> grantedAuthorities.add(new SimpleGrantedAuthority(a.getName())));
        }
        return grantedAuthorities;
    }


    @Transactional
    public void addUserDetails(AuthUserRequest authUserRequest) {
        logger.info("AUTHORIZATION-SERVER > [addUserDetails] - start");
        UserAuth user = customUserDetailsRepository.findByUsername(authUserRequest.getUsername());
        if (user != null) {
            log.warn("AUTHORIZATION-SERVER > [addUserDetails] - user {} already exists", authUserRequest.getUsername());
            return;
        }

        if (StringUtils.isBlank(authUserRequest.getRoles())) {
            log.error("AUTHORIZATION-SERVER > [addUserDetails] - roles field is missing, username={}", authUserRequest.getUsername());
            throw new BadRequestException(MISSING_ROLES_FIELD);
        }

        Collection<Authority> authorities = new ArrayList<>();
        for (String role : authUserRequest.getRoles().split(",")) {
            Authority authority = authorityRepository.findByName(role);
            if (authority != null) {
                authorities.add(authority);
            }
        }

        user = mapper.map(authUserRequest);
        user.setAuthorities(authorities);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        customUserDetailsRepository.save(user);
        logger.info("AUTHORIZATION-SERVER > [addUserDetails] - success");
    }

    public void delete(String username) {
        logger.info("AUTHORIZATION-SERVER > [delete] - start");
        UserAuth user = customUserDetailsRepository.findByUsername(username);
        if (user == null) {
            logger.warn("AUTHORIZATION-SERVER > [delete] - cannot find user " + username);
            return;
        }
        customUserDetailsRepository.delete(user);
        logger.info("AUTHORIZATION-SERVER > [delete] - success");
    }


    @Transactional
    public void updateUser(AuthUpdateUserRequest authUpdateUserRequest) {
        logger.info("AUTHORIZATION-SERVER > [update user] - start");
        UserAuth user = customUserDetailsRepository.findByUsername(authUpdateUserRequest.getUsername());
        if (user == null) {
            log.error("AUTHORIZATION-SERVER > [update user] - user not found, username={}", authUpdateUserRequest.getUsername());
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        String currentPassword = authUpdateUserRequest.getCurrentPassword();
        String newPassword = authUpdateUserRequest.getNewPassword();
        if (StringUtils.isNotBlank(currentPassword) && StringUtils.isNotBlank(newPassword)) {
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                log.error("AUTHORIZATION-SERVER > [update user] - wrong password, username={}", authUpdateUserRequest.getUsername());
                throw new BadCredentialsException(WRONG_PASSWORD);
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (StringUtils.isNotBlank(authUpdateUserRequest.getEmail())) {
            user.setEmail(authUpdateUserRequest.getEmail());
        }

        logger.info("AUTHORIZATION-SERVER > [update user] - success");
    }


    @Transactional
    public void responsiblePwdRecovery(String username, String responsibleNewPassword) {
        logger.info("AUTHORIZATION-SERVER > [responsiblePwdRecovery] - start");

        UserAuth user = customUserDetailsRepository.findByUsername(username);
        if (user == null) {
            log.error("AUTHORIZATION-SERVER > [responsiblePwdRecovery] - user not found, username={}", username);
            throw new ResourceNotFoundException(USER_NOT_FOUND);
        }

        if (StringUtils.isNotBlank(responsibleNewPassword)) {
            user.setPassword(passwordEncoder.encode(responsibleNewPassword));
        }

        logger.info("AUTHORIZATION-SERVER > [responsiblePwdRecovery] - success");
    }
}