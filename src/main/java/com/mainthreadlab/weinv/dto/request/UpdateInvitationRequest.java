package com.mainthreadlab.weinv.dto.request;

import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UpdateInvitationRequest {

    @NotNull(message = "must not be empty or null")
    private InvitationStatus status;

}
