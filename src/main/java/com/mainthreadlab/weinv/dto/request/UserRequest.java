package com.mainthreadlab.weinv.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.mainthreadlab.weinv.model.enums.EventType;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.enums.Role;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import com.mainthreadlab.weinv.validator.ContactNumberConstraint;
import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserRequest {

    @NotBlank(message = "must not be empty or null")
    private String username;

    //@NotBlank(message = "must not be empty or null")
    private String lastName;

    //@NotBlank(message = "must not be empty or null")
    private String firstName;

    @ContactNumberConstraint
    /** only italy mobile phone */
    //@NotBlank(message = "must not be empty or null")
    //@Pattern(regexp = "^(\\((00|\\+)39\\)|(00|\\+)39)?(38[890]|34[7-90]|36[680]|33[3-90]|32[89])\\d{7}$", message = "Wrong italian mobile phone number")
    private String phoneNumber;

    @Email
    //@NotBlank(message = "must not be empty or null")
    private String email;

    @ToString.Exclude
    @NotBlank(message = "must not be empty or null")
    /*@Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$",
      message = "Password must contain at least one lowercase, uppercase, digit or symbols character" +
     "and must be at least 8 characters in length")*/
    @Pattern(regexp = "^(?=.*[a-zA-Z]).{8,20}$", message = "Password must contain at least 8 characters in length")
    private String password;

    private Language language;

    //@NotBlank(message = "must not be empty or null")
    //@Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private Role role = Role.GUEST;

    private Integer tableNumber;

    private EventType eventType;

    // amount paid for the services: responsible
    private Double price;

    private Integer totalInvitations;

    //@Builder.Default
    @JsonSetter(nulls = Nulls.SKIP)
    private InvitationStatus status = InvitationStatus.NOT_REPLIED;

}
