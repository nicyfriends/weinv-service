package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.response.UserResponse;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.enums.Role;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mapper
public interface UserMapper {

    default User toEntity(UserRequest userRequest) {
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setUsername(userRequest.getUsername());
        user.setEventType(userRequest.getEventType());
        user.setLanguage(userRequest.getLanguage() != null ? userRequest.getLanguage() : Language.FR);
        user.setRoles(userRequest.getRole() != null ? userRequest.getRole().getDescription() : Role.GUEST.getDescription());
        user.setPrice(userRequest.getPrice());  // only for responsible
        user.setEnabled(true);
        return user;
    }

    default UserResponse toModel(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setLanguage(user.getLanguage().name());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhoneNumber(user.getPhoneNumber());
        userResponse.setUsername(user.getUsername());
        userResponse.setUuid(user.getUuid());
        userResponse.setEventType(user.getEventType());
        List<String> roles = Arrays.asList(user.getRoles().split(","));
        userResponse.setRoles(roles.stream().map(this::mapRole).toList());
        return userResponse;
    }

    // for UI
    default String mapRole(String role) {
        if (StringUtils.isNotBlank(role)) {
            if (role.contains("user")) return "R";
            else if (role.contains("guest")) return "G";
            else if (role.contains("admin")) return "A";
        }
        return "";
    }

    default AuthUserRequest toAuthUser(UserRequest userRequest) {
        AuthUserRequest authUserRequest = new AuthUserRequest();
        authUserRequest.setEmail(userRequest.getEmail());
        authUserRequest.setUsername(userRequest.getUsername());
        authUserRequest.setPassword(userRequest.getPassword());
        authUserRequest.setRoles(userRequest.getRole() != null ? userRequest.getRole().getDescription() : Role.GUEST.getDescription());
        return authUserRequest;
    }

}
