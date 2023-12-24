package com.mainthreadlab.weinv.dto.security;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class AuthUpdateUserRequest {

    @Email
    private String email;

    @NotEmpty(message = "must not be empty or null")
    private String username;

    private String currentPassword;

    private String newPassword;
}
