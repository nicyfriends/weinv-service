package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-27T18:29:16+0100",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 20 (Oracle Corporation)"
)
@Component
public class WeddingMapperImpl extends WeddingMapper {

    @Override
    public Wedding toEntity(WeddingRequest weddingRequest) {
        if ( weddingRequest == null ) {
            return null;
        }

        Wedding wedding = new Wedding();

        wedding.setType( weddingRequest.getType() );
        wedding.setWifeName( weddingRequest.getWifeName() );
        wedding.setHusbandName( weddingRequest.getHusbandName() );
        wedding.setDate( weddingRequest.getDate() );
        wedding.setCeremonyStartime( weddingRequest.getCeremonyStartime() );
        wedding.setCeremonyVenue( weddingRequest.getCeremonyVenue() );
        wedding.setReceptionStartime( weddingRequest.getReceptionStartime() );
        wedding.setReceptionVenue( weddingRequest.getReceptionVenue() );
        wedding.setMaxTables( weddingRequest.getMaxTables() );
        wedding.setDeadlineConfirmInvitation( weddingRequest.getDeadlineConfirmInvitation() );
        wedding.setInvitationText( weddingRequest.getInvitationText() );
        wedding.setInvitationOtherText( weddingRequest.getInvitationOtherText() );
        wedding.setGiftDetails( weddingRequest.getGiftDetails() );
        wedding.setEventOwnerFirstName( weddingRequest.getEventOwnerFirstName() );
        wedding.setEventOwnerLastName( weddingRequest.getEventOwnerLastName() );
        wedding.setEventName( weddingRequest.getEventName() );

        setAfterMappingToEntity( weddingRequest, wedding );

        return wedding;
    }

    @Override
    public WeddingResponse toModel(Wedding wedding, Integer numberOfSeatsTaken) {
        if ( wedding == null && numberOfSeatsTaken == null ) {
            return null;
        }

        WeddingResponse weddingResponse = new WeddingResponse();

        if ( wedding != null ) {
            weddingResponse.setResponsibleUUID( weddingResponsibleUuid( wedding ) );
            weddingResponse.setUuid( wedding.getUuid() );
            weddingResponse.setMaxInvitations( wedding.getMaxInvitations() );
            weddingResponse.setMaxTables( wedding.getMaxTables() );
            weddingResponse.setDate( wedding.getDate() );
            weddingResponse.setWifeName( wedding.getWifeName() );
            weddingResponse.setHusbandName( wedding.getHusbandName() );
            weddingResponse.setCeremonyVenue( wedding.getCeremonyVenue() );
            weddingResponse.setReceptionVenue( wedding.getReceptionVenue() );
            weddingResponse.setDeadlineConfirmInvitation( wedding.getDeadlineConfirmInvitation() );
            weddingResponse.setInvitationText( wedding.getInvitationText() );
            weddingResponse.setInvitationOtherText( wedding.getInvitationOtherText() );
            weddingResponse.setUrlVideo( wedding.getUrlVideo() );
            weddingResponse.setGiftDetails( wedding.getGiftDetails() );
            weddingResponse.setEventOwnerFirstName( wedding.getEventOwnerFirstName() );
            weddingResponse.setEventOwnerLastName( wedding.getEventOwnerLastName() );
            weddingResponse.setEventName( wedding.getEventName() );
            weddingResponse.setEventType( wedding.getEventType() );
        }

        setAfterMappingToModel( wedding, numberOfSeatsTaken, weddingResponse );

        return weddingResponse;
    }

    private String weddingResponsibleUuid(Wedding wedding) {
        if ( wedding == null ) {
            return null;
        }
        User responsible = wedding.getResponsible();
        if ( responsible == null ) {
            return null;
        }
        String uuid = responsible.getUuid();
        if ( uuid == null ) {
            return null;
        }
        return uuid;
    }
}
