package com.mainthreadlab.weinv.dto.request;

import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import com.mainthreadlab.weinv.model.enums.Language;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UpdateUserRequest {

    private String uuid;               // for the guest

    @Email
    private String email;              // both

    private String lastName;           // both

    private String firstName;          // both

    private String phoneNumber;        // both

    private Language language;      // both

    private Integer totalInvitations; //

    private Integer tableNumber;          // for the guest

    private String currentPassword;     // for the responsible

    private String newPassword;         // for the responsible

}
