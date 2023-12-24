package com.mainthreadlab.weinv.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class ContactUsRequest {

    @NotBlank(message = "must not be empty or null")
    private String name;

    @Email
    @NotBlank(message = "must not be empty or null")
    private String from;

    private String phoneNumber;

    @NotBlank(message = "must not be empty or null")
    private String message;

}
