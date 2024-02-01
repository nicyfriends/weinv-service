package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.dto.request.UpdateUserRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.response.UserResponse;
import com.mainthreadlab.weinv.dto.security.AuthUpdateUserRequest;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.enums.Role;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {

    default User toEntity(UserRequest userRequest) {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setUsername(userRequest.getUsername());
        user.setEventType(userRequest.getEventType());
        user.setLanguage(userRequest.getLanguage() != null ? userRequest.getLanguage() : Language.FR);
        user.setRoles(userRequest.getRole() != null ? userRequest.getRole().getDescription() : Role.GUEST.getDescription());
        user.setPrice(userRequest.getPrice());  // only for responsible
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(true);
        return user;
    }

    default UserResponse toModel(User user) {
        List<String> roles = Arrays.asList(user.getRoles().split(","));
        return new UserResponse()
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setLanguage(user.getLanguage().name())
                .setEmail(user.getEmail())
                .setPhoneNumber(user.getPhoneNumber())
                .setUsername(user.getUsername())
                .setUuid(user.getUuid())
                .setEventType(user.getEventType())
                .setRoles(roles.stream().map(this::mapRole).toList());
    }

    // for UI
    default String mapRole(String role) {
        if (StringUtils.isNotBlank(role)) {
            if (role.contains("user")) return "R";
            else if (role.contains("guest")) return "G";
            else if (role.contains("admin")) return "A";
        }
        return Strings.EMPTY;
    }

    default AuthUserRequest toAuthUser(UserRequest userRequest) {
        AuthUserRequest authUserRequest = new AuthUserRequest();
        authUserRequest.setEmail(userRequest.getEmail());
        authUserRequest.setUsername(userRequest.getUsername());
        authUserRequest.setPassword(userRequest.getPassword());
        authUserRequest.setRoles(userRequest.getRole() != null ? userRequest.getRole().getDescription() : Role.GUEST.getDescription());
        return authUserRequest;
    }

    AuthUpdateUserRequest map(UpdateUserRequest updateUserRequest);
}
