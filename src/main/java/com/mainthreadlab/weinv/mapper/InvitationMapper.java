package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.base.InvitationId;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import org.mapstruct.Mapper;

import java.util.Arrays;

@Mapper
public interface InvitationMapper {

    default Invitation toEntity(Wedding wedding, User guest, UserRequest request) {
        Invitation invitation = new Invitation();
        InvitationId invitationId = new InvitationId();
        invitationId.setWeddingUuid(wedding.getUuid());
        invitationId.setGuestUuid(guest.getUuid());
        invitation.setId(invitationId);
        invitation.setWedding(wedding);
        invitation.setGuest(guest);
        invitation.setTableNumber(request.getTableNumber());
        invitation.setStatus(request.getStatus());
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
        invitationResponse.setHusband(invitation.getGuest().getHusband());
        invitationResponse.setWife(invitation.getGuest().getWife());
        invitationResponse.setCouple(invitation.getGuest().isCouple());
        invitationResponse.setPhoneNumber(invitation.getGuest().getPhoneNumber());
        invitationResponse.setTableNumber(invitation.getTableNumber());
        invitationResponse.setUsername(invitation.getGuest().getUsername());
        invitationResponse.setUuid(invitation.getGuest().getUuid());
        invitationResponse.setUuidWedding(invitation.getWedding().getUuid());
        invitationResponse.setEventType(invitation.getGuest().getEventType());
        invitationResponse.setTotalInvitations(invitation.getTotalInvitations());
        return invitationResponse;
    }
}
