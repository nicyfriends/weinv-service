package com.mainthreadlab.weinv.service;

import com.itextpdf.text.DocumentException;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.dto.request.ConfirmRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.request.WeddingUpdateRequest;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface WeddingService {

    String createWedding(WeddingRequest weddingRequest);

    void invite(String uuid, UserRequest userRequest) throws URISyntaxException, IOException;

    WeddingResponse getWedding(String uuid);

    Page<InvitationResponse> getWeddingInvitations(String uuidWedding, String searchKeyword, Pageable pageable);

    List<WeddingResponse> getWeddings(Pageable pageable);

    void updateWedding(String uuid, WeddingUpdateRequest weddingRequest, JwtDetails jwtDetails);

    void deleteWedding(String uuid);

    void confirmInvitation(ConfirmRequest confirmRequest, String uuidWedding, String uuidGuest);

    void downloadPdf(String uuidWedding, HttpServletResponse httpResponse) throws DocumentException, IOException;

    Wedding getByResponsible(User responsible);

    Wedding getByUuid(String uuid);
}
