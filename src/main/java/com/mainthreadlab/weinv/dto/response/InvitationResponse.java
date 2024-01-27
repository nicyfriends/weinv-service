package com.mainthreadlab.weinv.dto.response;

import com.mainthreadlab.weinv.model.enums.EventType;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class InvitationResponse {

    private String uuid;
    private String username;
    private String lastName;
    private String firstName;
    private String wife;
    private String husband;
    private boolean couple;
    private String phoneNumber;
    private String email;
    private List<String> roles;
    private Integer tableNumber;
    private String language;
    private EventType eventType;
    private Integer totalInvitations;
    private InvitationStatus status;
    private String uuidWedding;

}
