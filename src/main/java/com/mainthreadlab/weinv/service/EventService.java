package com.mainthreadlab.weinv.service;

import com.itextpdf.text.DocumentException;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.dto.request.UpdateInvitationRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.request.WeddingUpdateRequest;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.ResponsePage;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface EventService {

    String createWedding(WeddingRequest weddingRequest);

    void invite(String uuid, UserRequest userRequest) throws URISyntaxException, IOException;

    WeddingResponse getWedding(String uuid);

    ResponsePage<InvitationResponse> getWeddingInvitations(String uuidWedding, String searchKeyword, Pageable pageable, InvitationStatus invitationStatus);

    List<WeddingResponse> getWeddings(Pageable pageable);

    void updateWedding(String uuid, WeddingUpdateRequest weddingRequest, JwtDetails jwtDetails);

    void deleteWedding(String uuid);

    void updateInvitationStatus(UpdateInvitationRequest updateInvitationRequest, String uuidWedding, String uuidGuest);

    void downloadPdf(String uuidWedding, HttpServletResponse httpResponse) throws DocumentException, IOException;

    Event getByResponsible(User responsible);

    Event getByUuid(String uuid);

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
