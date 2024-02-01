package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.base.InvitationId;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import org.mapstruct.Mapper;

import java.util.Arrays;

@Mapper
public interface InvitationMapper {

    default Invitation toEntity(Event event, User guest, UserRequest request) {
        Invitation invitation = new Invitation();
        InvitationId invitationId = new InvitationId();
        invitationId.setEventUuid(event.getUuid());
        invitationId.setGuestUuid(guest.getUuid());
        invitation.setId(invitationId);
        invitation.setEvent(event);
        invitation.setGuest(guest);
        invitation.setTableNumber(request.getTableNumber());
        invitation.setStatus(request.getStatus());
        invitation.setTotalInvitations(request.getTotalInvitations());
        return invitation;
    }


    default InvitationResponse toInvitation(Invitation invitation) {
        InvitationResponse invitationResponse = new InvitationResponse();
        invitationResponse.setStatus(invitation.getStatus());
        invitationResponse.setRoles(Arrays.asList(invitation.getGuest().getRoles().split(",")));
        invitationResponse.setLanguage(invitation.getGuest().getLanguage().name());
        invitationResponse.setEmail(invitation.getGuest().getEmail());
        invitationResponse.setFirstName(invitation.getGuest().getFirstName());
        invitationResponse.setLastName(invitation.getGuest().getLastName());
        invitationResponse.setPhoneNumber(invitation.getGuest().getPhoneNumber());
        invitationResponse.setTableNumber(invitation.getTableNumber());
        invitationResponse.setUsername(invitation.getGuest().getUsername());
        invitationResponse.setUuid(invitation.getGuest().getUuid());
        invitationResponse.setUuidEvent(invitation.getEvent().getUuid());
        invitationResponse.setEventType(invitation.getGuest().getEventType());
        invitationResponse.setTotalInvitations(invitation.getTotalInvitations());
        return invitationResponse;
    }
}
