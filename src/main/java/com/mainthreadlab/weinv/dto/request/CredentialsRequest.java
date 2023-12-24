package com.mainthreadlab.weinv.dto.request;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
public class CredentialsRequest {

    @NotBlank(message = "must not be empty or null")
    private String username;

    @ToString.Exclude
    @NotBlank(message = "must not be empty or null")
    private String password;

}
