package com.mainthreadlab.weinv.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mainthreadlab.weinv.commons.Utils;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.dto.request.UpdateInvitationRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.request.WeddingUpdateRequest;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.ResponsePage;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.exception.BadRequestException;
import com.mainthreadlab.weinv.exception.ResourceNotFoundException;
import com.mainthreadlab.weinv.mapper.UserMapper;
import com.mainthreadlab.weinv.mapper.InvitationMapper;
import com.mainthreadlab.weinv.mapper.WeddingMapper;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.repository.InvitationRepository;
import com.mainthreadlab.weinv.repository.WeddingRepository;
import com.mainthreadlab.weinv.service.EmailService;
import com.mainthreadlab.weinv.service.UserService;
import com.mainthreadlab.weinv.service.WeddingService;
import com.mainthreadlab.weinv.service.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

import static com.mainthreadlab.weinv.commons.Constants.*;
import static com.mainthreadlab.weinv.commons.Utils.isSourceDateBeforeTargetDate;
import static com.mainthreadlab.weinv.model.enums.ErrorKey.*;
import static com.mainthreadlab.weinv.model.enums.EventType.WEDDING;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WeddingServiceImpl implements WeddingService {

    private final WeddingRepository weddingRepository;
    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailSender;
    private final WeddingMapper weddingMapper;
    private final UserMapper userMapper;
    private final InvitationMapper invitationMapper;

    @Value("${weinv.ui.login-uri}")
    private String uiLoginUri;

    @Value("${weinv.mail.events.wedding.body.fr}")
    private String frInvitationBodyFilePath;

    @Value("${weinv.mail.events.wedding.body.en}")
    private String enInvitationBodyFilePath;

    @Value("${weinv.mail.events.wedding.subject}")
    private String invitationSubject;


    @Override
    public String createWedding(WeddingRequest weddingRequest) {

        log.info("[create wedding] - start: {}", weddingRequest.toString());

        if (weddingRequest.getDate() != null && isSourceDateBeforeTargetDate(weddingRequest.getDate(), new Date())) {
            log.error("[create wedding] - wedding date must not be before the current date");
            throw new BadRequestException(INVALID_WEDDING_DATE);
        }

        weddingRequest.setHusbandName(StringUtils.capitalize(weddingRequest.getHusbandName()));
        weddingRequest.setWifeName(StringUtils.capitalize(weddingRequest.getWifeName()));

        User responsible = userService.getByUuid(weddingRequest.getResponsibleUUID());
        if (responsible == null) {
            log.error("[create wedding] - wedding responsible not found, uuid = {}", weddingRequest.getResponsibleUUID());
            throw new ResourceNotFoundException(WEDDING_RESPONSIBLE_NOT_FOUND);
        }

        //control if the user is responsible for another wedding
        Wedding wedding = getByResponsible(responsible);
        if (wedding != null) {
            log.error("[create wedding] - user {} is already responsible for another wedding", responsible.getUsername());
            throw new BadRequestException(USER_ALREADY_RESPONSIBLE);
        }

        wedding = weddingMapper.toEntity(weddingRequest);
        wedding.setResponsible(responsible);
        wedding.setEventType(responsible.getEventType());
        wedding = weddingRepository.save(wedding);

        log.info("[create wedding] - end");
        return wedding.getUuid();

    }


    @Override
    @Transactional
    public void invite(String uuid, UserRequest request) {

        log.info("[invite] - start: {}", request.toString());

        Wedding wedding = getByUuid(uuid);
        if (wedding == null) {
            log.error("[invite] - wedding not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        if (Objects.nonNull(wedding.getMaxInvitations())) {
            int invitationsAlreadySent = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding).size();
            if (wedding.getMaxInvitations() == invitationsAlreadySent) {
                log.error("[invite] - maximum number of invitations reached, uuid = {}", uuid);
                throw new BadRequestException(MAX_INVITATION_NUMBER_REACHED);
            }
        }

        if (Objects.nonNull(request.getTableNumber()) &&
                Objects.nonNull(wedding.getMaxTables()) &&
                (request.getTableNumber() > wedding.getMaxTables())) {
            log.error("[invite] - table number incorrect, uuid = {}", uuid);
            throw new BadRequestException(INCORRECT_TABLE_NUMBER);
        }

        request.setEventType(wedding.getEventType());
        request.setFirstName(StringUtils.capitalize(request.getFirstName()));
        request.setLastName(StringUtils.capitalize(request.getLastName()));
        User user = userService.save(request);

        invitationRepository.save(invitationMapper.toEntity(wedding, user, request));

        wedding.setTotalGuestsNotReplied(wedding.getTotalGuestsNotReplied() + request.getTotalInvitations());

        log.info("[invite] - save in authorization-server");
        AuthUserRequest authUserRequest = userMapper.toAuthUser(request);
        customUserDetailsService.addUserDetails(authUserRequest);

        log.info("[invite] - sending invitation mail...");
        sendMail(wedding, request);

        log.info("[invite] - end");
    }

    @Override
    public WeddingResponse getWedding(String uuid) {

        log.info("[get wedding] - start: uuid={}", uuid);

        WeddingResponse response = null;
        Wedding wedding = getByUuid(uuid);
        if (wedding != null) {
            List<Invitation> invitationList = invitationRepository.findByWedding(wedding);
            response = weddingMapper.toModel(wedding, invitationList.size());
        }

        log.info("[get wedding] - end");
        return response;

    }

    // also search
    @Override
    public ResponsePage<InvitationResponse> getWeddingInvitations(String uuidWedding, String searchKeyword, Pageable pageable, InvitationStatus invitationStatus) {

        log.info("[get wedding invitations] - start: uuidWedding={}", uuidWedding);

        Specification<Invitation> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.orderBy(criteriaBuilder.asc(root.get(GUEST_FIELD).get(FIRSTNAME_FIELD)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(WEDDING_FIELD).get(UUID_FIELD), uuidWedding),
                    criteriaBuilder.greaterThanOrEqualTo(root.get(WEDDING_FIELD).get(WEDDING_DATE_FIELD), new Date())));

            if (Objects.nonNull(invitationStatus)) {
                predicates.add(criteriaBuilder.equal(root.get(INVITATION_STATUS_FIELD), invitationStatus));
            }
            if (StringUtils.isNotBlank(searchKeyword)) {
                String keyword = LIKE_KEYWORD_FORMAT.replace("keyword", searchKeyword);
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get(GUEST_FIELD).get(USERNAME_FIELD), keyword),
                        criteriaBuilder.like(root.get(GUEST_FIELD).get(FIRSTNAME_FIELD), keyword),
                        criteriaBuilder.like(root.get(GUEST_FIELD).get(LASTNAME_FIELD), keyword)));
            }
            predicates.add(criteriaBuilder.isTrue(root.get(GUEST_FIELD).get(ENABLED_FIELD)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
        Page<Invitation> invitationsPage = invitationRepository.findAll(specification, pageable);

        ResponsePage<InvitationResponse> responsePage = new ResponsePage<>(invitationsPage.map(invitationMapper::toInvitation));
        if (!invitationsPage.isEmpty()) {
            Wedding wedding = invitationsPage.getContent().get(0).getWedding();
            responsePage.setTotalGuestsAttending(wedding.getTotalGuestsAttending());
            responsePage.setTotalGuestsNotAttending(wedding.getTotalGuestsNotAttending());
            responsePage.setTotalGuestsMaybe(wedding.getTotalGuestsMaybe());
            responsePage.setTotalGuestsNotReplied(wedding.getTotalGuestsNotReplied());
        }

        log.info("[get wedding invitations] - found {} element(s)", invitationsPage.getSize());
        log.info("[get wedding invitations] - end");

        return responsePage;
    }


    @Override
    public List<WeddingResponse> getWeddings(Pageable pageable) {

        log.info("[get weddings] - start");

        Page<Wedding> pageWeddings = weddingRepository.findAll(pageable);
        List<WeddingResponse> response = pageWeddings
                .stream()
                .map(w -> weddingMapper.toModel(w, invitationRepository.findByWedding(w).size()))
                .toList();

        // remove unnecessary data (QoS)
        response.forEach(w -> w.setSpousesImage(null));

        log.info("[get weddings] - found {} element(s)", response.size());
        log.info("[get weddings] - end");
        return response;
    }

    @Override
    @Transactional
    public void updateWedding(String uuid, WeddingUpdateRequest weddingUpdateRequest, JwtDetails jwtDetails) {

        log.info("[update wedding] - start: uuid={}", uuid);

        if (weddingUpdateRequest.getDate() != null && isSourceDateBeforeTargetDate(weddingUpdateRequest.getDate(), new Date())) {
            log.error("[update wedding] - wedding date must not be before the current date");
            throw new BadRequestException(INVALID_WEDDING_DATE);
        }

        if (StringUtils.isNotBlank(weddingUpdateRequest.getHusbandName())) {
            weddingUpdateRequest.setHusbandName(StringUtils.capitalize(weddingUpdateRequest.getHusbandName()));
        }
        if (StringUtils.isNotBlank(weddingUpdateRequest.getWifeName())) {
            weddingUpdateRequest.setWifeName(StringUtils.capitalize(weddingUpdateRequest.getWifeName()));
        }

        Wedding wedding = weddingRepository.findByUuid(uuid);
        if (wedding == null) {
            log.error("[update wedding] - wedding not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        User responsible = userService.getByUuid(weddingUpdateRequest.getResponsibleUUID());
        if (responsible == null) {
            log.error("[update wedding] - wedding responsible not found, uuid = {}", weddingUpdateRequest.getResponsibleUUID());
            throw new ResourceNotFoundException(WEDDING_RESPONSIBLE_NOT_FOUND);
        }

        this.updateWeddingPrice(weddingUpdateRequest, jwtDetails, responsible);
        this.responsiblePwdRecovery(weddingUpdateRequest, jwtDetails, responsible);
        this.updateWedding(wedding, responsible, weddingUpdateRequest);

        log.info("[update wedding] - end");

    }

    private void updateWeddingPrice(WeddingUpdateRequest weddingRequest, JwtDetails jwtDetails, User responsible) {
        log.info("[update wedding price] - start");

        if (jwtDetails != null && jwtDetails.getAuthorities() != null && !jwtDetails.getAuthorities().isEmpty()) {
            List<String> authorities = jwtDetails.getAuthorities();
            if (authorities.contains("admin") && weddingRequest.getPrice() != null) {
                responsible.setPrice(weddingRequest.getPrice());
            }
        }
        log.info("[update wedding price] - end");
    }

    private void responsiblePwdRecovery(WeddingUpdateRequest weddingRequest, JwtDetails jwtDetails, User responsible) {
        log.info("[responsible password recovery] - start");

        if (jwtDetails != null && jwtDetails.getAuthorities() != null && !jwtDetails.getAuthorities().isEmpty()) {
            List<String> authorities = jwtDetails.getAuthorities();
            if (authorities.contains("admin") && StringUtils.isNotBlank(weddingRequest.getResponsibleNewPassword())) {
                customUserDetailsService.responsiblePwdRecovery(responsible.getUsername(), weddingRequest.getResponsibleNewPassword());
            }
        }
        log.info("[responsible password recovery] - end");
    }

    @Override
    public void deleteWedding(String uuidWedding) {
        log.info("[delete wedding] - start: uuid={}", uuidWedding);

        Wedding wedding = getByUuid(uuidWedding);
        if (wedding == null) {
            wedding = weddingRepository.findByUuid(uuidWedding);   // admin: delete (no matters date)
            if (wedding == null) {
                log.error("[delete wedding] - wedding not found, uuid = {}", uuidWedding);
                throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
            }
        }
        List<Invitation> invitationList = invitationRepository.findByWedding(wedding);
        invitationRepository.deleteAll(invitationList);   // delete all invitations
        invitationList.forEach(wg -> userService.deleteGuestInvitation(wg.getGuest().getUuid(), uuidWedding));  // delete guests
        userService.deleteGuestInvitation(wedding.getResponsible().getUuid(), uuidWedding);   // delete responsible
        weddingRepository.delete(wedding);   // delete wedding

        log.info("[delete wedding] - end");

    }

    @Override
    public void updateInvitationStatus(UpdateInvitationRequest request, String uuidWedding, String uuidGuest) {

        log.info("[update invitation status] - start: uuidWedding={}, uuiGuest={}", uuidWedding, uuidGuest);

        Wedding wedding = getByUuid(uuidWedding);
        if (wedding == null) {
            log.error("[update invitation status] - wedding not found, uuid = {}", uuidWedding);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        User guest = userService.getByUuid(uuidGuest);
        if (guest == null) {
            log.error("[update invitation status] - user not found, uuid = {}", uuidGuest);
            throw new ResourceNotFoundException(USER_ALREADY_RESPONSIBLE);
        }

        Invitation invitation = invitationRepository.findByWeddingAndGuest(wedding, guest);
        if (invitation == null) {
            log.error("[update invitation status] - invitation not found, uuidWedding={}, uuiGuest={}", uuidWedding, uuidWedding);
            throw new ResourceNotFoundException(INVITATION_NOT_FOUND);
        }

        WeddingService.updateStatusInvitationNumber(wedding, invitation.getStatus(), invitation.getTotalInvitations(), "-");
        WeddingService.updateStatusInvitationNumber(wedding, request.getStatus(), invitation.getTotalInvitations(), "+");

        invitation.setStatus(request.getStatus());

        log.info("[update invitation status] - end");
    }

    /**
     * return pdf file that contains list of guests of a wedding
     */
    @Override
    public void downloadPdf(String uuidWedding, HttpServletResponse httpResponse) throws DocumentException, IOException {

        log.info("[download pdf] - start: uuidWedding={}", uuidWedding);

        Wedding wedding = getByUuid(uuidWedding);
        if (wedding == null) {
            log.error("[download pdf] - wedding not found, uuid = {}", uuidWedding);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        Document document = new Document();
        PdfWriter.getInstance(document, httpResponse.getOutputStream());
        document.open();
        PdfPTable table = new PdfPTable(3);
        if (wedding.getEventType() == WEDDING) {
            addTableTileAndHeaderWeddingEvent(table, wedding);
        } else {
            addTableTileAndHeader(table, wedding);
        }
        addRows(table, wedding);
        document.add(table);
        document.close();

        log.info("[download pdf] - end");

    }

    @Override
    public Wedding getByResponsible(User responsible) {
        Specification<Wedding> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("responsible").get("uuid"), responsible.getUuid()));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), new Date()));
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
        return weddingRepository.findAll(specification).stream().findFirst().orElse(null);
    }

    @Override
    public Wedding getByUuid(String uuid) {
        return weddingRepository.findByUuidAndDateGreaterThanEqual(uuid, new Date());
    }

    private void addRows(PdfPTable table, Wedding wedding) {
        List<Invitation> invitationList = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding);
        for (Invitation wg : invitationList) {
            table.addCell(getRowPdfPCell(wg.getGuest().getFirstName() + " " + wg.getGuest().getLastName()));
            table.addCell(getRowPdfPCell(wg.getTableNumber() != null ? wg.getTableNumber().toString() : null));
            table.addCell(getRowPdfPCell(wg.getStatus().getDescription()));
        }
    }

    private PdfPCell getRowPdfPCell(String input) {
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(input, font));
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(3.0f);
        return pdfPCell;
    }

    private void addTableTileAndHeader(PdfPTable table, Wedding wedding) {
        // title
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        String title = INVITATION_PDF_MAIN_TITLE.formatted(wedding.getEventOwnerFirstName().toUpperCase(), wedding.getEventOwnerLastName().toUpperCase());
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(title, boldFont));
        pdfPCell.setColspan(4);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(20.0f);
        table.addCell(pdfPCell);

        List<String> columns = Arrays.asList(NOM, TABLE_NO, PRESENCE);

        // seats information
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        List<Invitation> invitationList = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding);
        int seatsAvailable = wedding.getMaxInvitations() - invitationList.size();

        pdfPCell = new PdfPCell(new Paragraph(INVITATION_PDF_TITLE.formatted(wedding.getMaxInvitations(), invitationList.size(), seatsAvailable), font));
        pdfPCell.setColspan(4);
        pdfPCell.setPadding(10.0f);
        table.addCell(pdfPCell);

        for (String columnTitle : columns) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setBorderWidth(2);
            header.setPadding(10.0f);
            header.setPhrase(new Phrase(columnTitle, boldFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        }
    }

    private void addTableTileAndHeaderWeddingEvent(PdfPTable table, Wedding wedding) {
        // title
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        String title = W_INVITATION_PDF_MAIN_TITLE.formatted(wedding.getHusbandName().toUpperCase(), wedding.getWifeName().toUpperCase());
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(title, boldFont));
        pdfPCell.setColspan(4);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(20.0f);
        table.addCell(pdfPCell);

        List<String> columns = Arrays.asList(NOM, TABLE_NO, PRESENCE);

        // seats information
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        List<Invitation> invitationList = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding);
        int seatsAvailable = wedding.getMaxInvitations() - invitationList.size();

        pdfPCell = new PdfPCell(new Paragraph(INVITATION_PDF_TITLE.formatted(wedding.getMaxInvitations(), invitationList.size(), seatsAvailable), font));
        pdfPCell.setColspan(4);
        pdfPCell.setPadding(10.0f);
        table.addCell(pdfPCell);

        for (String columnTitle : columns) {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setBorderWidth(2);
            header.setPadding(10.0f);
            header.setPhrase(new Phrase(columnTitle, boldFont));
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(header);
        }
    }

    private void sendMail(Wedding wedding, UserRequest request) {
        try {
            String template = Utils.readFileFromResource(frInvitationBodyFilePath);
            if (Language.EN.equals(request.getLanguage())) {
                template = Utils.readFileFromResource(enInvitationBodyFilePath);
            }

            template = template.replace("{f_firstname}", wedding.getWifeName())
                    .replace("{m_firstname}", wedding.getHusbandName())
                    .replace("{login_uri}", uiLoginUri)
                    .replace("{username}", request.getUsername())
                    .replace("{password}", request.getPassword());

            // send to the guest
            emailSender.sendHtmlEmail(request.getEmail(), invitationSubject, template);
            // send a copy to the responsible for wedding
            emailSender.sendHtmlEmail(wedding.getResponsible().getEmail(), invitationSubject, template);
        } catch (Exception e) {
            log.error("[sendMail] - error sending email: {}", e.getMessage(), e);
        }

    }

    private void updateWedding(Wedding wedding, User responsible, WeddingUpdateRequest weddingRequest) {
        if (weddingRequest.getType() != null) wedding.setType(weddingRequest.getType());
        if (weddingRequest.getDate() != null) wedding.setDate(weddingRequest.getDate());
        if (weddingRequest.getDeadlineConfirmInvitation() != null)
            wedding.setDeadlineConfirmInvitation(weddingRequest.getDeadlineConfirmInvitation());
        if (StringUtils.isNotBlank(weddingRequest.getInvitationText()))
            wedding.setInvitationText(weddingRequest.getInvitationText());
        if (StringUtils.isNotBlank(weddingRequest.getInvitationOtherText()))
            wedding.setInvitationOtherText(weddingRequest.getInvitationOtherText());
        if (StringUtils.isNotBlank(weddingRequest.getWifeName())) wedding.setWifeName(weddingRequest.getWifeName());
        if (StringUtils.isNotBlank(weddingRequest.getHusbandName()))
            wedding.setHusbandName(weddingRequest.getHusbandName());
        if (StringUtils.isNotBlank(weddingRequest.getReceptionVenue()))
            wedding.setReceptionVenue(weddingRequest.getReceptionVenue());
        if (StringUtils.isNotBlank(weddingRequest.getGiftDetails()))
            wedding.setGiftDetails(weddingRequest.getGiftDetails());
        if (weddingRequest.getMaxInvitations() != null) wedding.setMaxInvitations(weddingRequest.getMaxInvitations());
        if (weddingRequest.getMaxTables() != null) wedding.setMaxTables(weddingRequest.getMaxTables());
        if (weddingRequest.getReceptionStartime() != null)
            wedding.setReceptionStartime(weddingRequest.getReceptionStartime());
        if (weddingRequest.getSpousesImage() != null) {
            wedding.setSpousesImage(weddingRequest.getSpousesImage().getBytes(StandardCharsets.UTF_8));
        }

        // others events
        if (weddingRequest.getEventName() != null) {
            wedding.setEventName(weddingRequest.getEventName());
        }
        if (weddingRequest.getEventOwnerLastName() != null) {
            wedding.setEventOwnerLastName(weddingRequest.getEventOwnerLastName());
        }
        if (weddingRequest.getEventOwnerFirstName() != null) {
            wedding.setEventOwnerFirstName(weddingRequest.getEventOwnerFirstName());
        }

        wedding.setCeremonyVenue(weddingRequest.getCeremonyVenue());
        wedding.setCeremonyStartime(weddingRequest.getCeremonyStartime());
        wedding.setResponsible(responsible);
    }

}
