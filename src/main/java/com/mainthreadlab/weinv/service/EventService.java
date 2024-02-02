package com.mainthreadlab.weinv.service;

import com.itextpdf.text.DocumentException;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.dto.request.UpdateInvitationRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.EventRequest;
import com.mainthreadlab.weinv.dto.request.EventUpdateRequest;
import com.mainthreadlab.weinv.dto.response.EventResponse;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.ResponsePage;
import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface EventService {

    String createEvent(EventRequest eventRequest);

    void invite(String uuid, UserRequest userRequest) throws URISyntaxException, IOException;

    EventResponse getEvent(String uuid);

    ResponsePage<InvitationResponse> getEventInvitations(String uuidEvent, String searchKeyword, Pageable pageable, InvitationStatus invitationStatus);

    List<EventResponse> getEvents(Pageable pageable);

    void updateEvent(String uuid, EventUpdateRequest weddingRequest, JwtDetails jwtDetails);

    void deleteEvent(String uuid);

    void updateInvitationStatus(UpdateInvitationRequest updateInvitationRequest, String uuidEvent, String uuidGuest);

    void downloadPdf(String uuidEvent, HttpServletResponse httpResponse) throws DocumentException, IOException;

    Event getByResponsible(User responsible);

    Event getByUuid(String uuid);

    String getEventImage(String uuid);

    static void updateStatusInvitationNumber(Event event, InvitationStatus status, Integer totalInvitations, String operation) {
        if ("-".equals(operation)) {
            switch (status) {
                case ATTENDING -> event.setTotalGuestsAttending(event.getTotalGuestsAttending() - totalInvitations);
                case NOT_ATTENDING ->
                        event.setTotalGuestsNotAttending(event.getTotalGuestsNotAttending() - totalInvitations);
                case MAYBE -> event.setTotalGuestsMaybe(event.getTotalGuestsMaybe() - totalInvitations);
                case NOT_REPLIED -> event.setTotalGuestsNotReplied(event.getTotalGuestsNotReplied() - totalInvitations);
            }
        }

        if ("+".equals(operation)) {
            switch (status) {
                case ATTENDING -> event.setTotalGuestsAttending(event.getTotalGuestsAttending() + totalInvitations);
                case NOT_ATTENDING ->
                        event.setTotalGuestsNotAttending(event.getTotalGuestsNotAttending() + totalInvitations);
                case MAYBE -> event.setTotalGuestsMaybe(event.getTotalGuestsMaybe() + totalInvitations);
                case NOT_REPLIED -> event.setTotalGuestsNotReplied(event.getTotalGuestsNotReplied() + totalInvitations);
            }
        }
    }

}
