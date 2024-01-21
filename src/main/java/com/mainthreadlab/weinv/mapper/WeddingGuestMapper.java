package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.WeddingGuest;
import com.mainthreadlab.weinv.model.base.WeddingGuestId;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import org.mapstruct.Mapper;

import java.util.Arrays;

@Mapper
public interface WeddingGuestMapper {

    default WeddingGuest toEntity(Wedding wedding, User guest, Integer tableNumber) {
        WeddingGuest weddingGuest = new WeddingGuest();
        WeddingGuestId weddingGuestId = new WeddingGuestId();
        weddingGuestId.setWeddingUuid(wedding.getUuid());
        weddingGuestId.setGuestUuid(guest.getUuid());
        weddingGuest.setId(weddingGuestId);
        weddingGuest.setWedding(wedding);
        weddingGuest.setGuest(guest);
        weddingGuest.setTableNumber(tableNumber);
        return weddingGuest;
    }


    default InvitationResponse toInvitation(WeddingGuest weddingGuest) {
        InvitationResponse invitationResponse = new InvitationResponse();
        invitationResponse.setConfirmed(weddingGuest.isConfirmed());
        invitationResponse.setRejected(weddingGuest.isRejected());
        invitationResponse.setRoles(Arrays.asList(weddingGuest.getGuest().getRoles().split(",")));
        invitationResponse.setLanguage(weddingGuest.getGuest().getLanguage().name());
        invitationResponse.setEmail(weddingGuest.getGuest().getEmail());
        invitationResponse.setFirstName(weddingGuest.getGuest().getFirstName());
        invitationResponse.setLastName(weddingGuest.getGuest().getLastName());
        invitationResponse.setHusband(weddingGuest.getGuest().getHusband());
        invitationResponse.setWife(weddingGuest.getGuest().getWife());
        invitationResponse.setCouple(weddingGuest.getGuest().isCouple());
        invitationResponse.setPhoneNumber(weddingGuest.getGuest().getPhoneNumber());
        invitationResponse.setTableNumber(weddingGuest.getTableNumber());
        invitationResponse.setUsername(weddingGuest.getGuest().getUsername());
        invitationResponse.setUuid(weddingGuest.getGuest().getUuid());
        invitationResponse.setUuidWedding(weddingGuest.getWedding().getUuid());
        invitationResponse.setEventType(weddingGuest.getGuest().getEventType());
        return invitationResponse;
    }
}
