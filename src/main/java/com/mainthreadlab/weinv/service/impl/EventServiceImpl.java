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
import com.mainthreadlab.weinv.mapper.EventMapper;
import com.mainthreadlab.weinv.mapper.InvitationMapper;
import com.mainthreadlab.weinv.mapper.UserMapper;
import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.Invitation;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.repository.EventRepository;
import com.mainthreadlab.weinv.repository.InvitationRepository;
import com.mainthreadlab.weinv.service.EmailService;
import com.mainthreadlab.weinv.service.EventService;
import com.mainthreadlab.weinv.service.UserService;
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
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailSender;
    private final EventMapper eventMapper;
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
        Event event = getByResponsible(responsible);
        if (event != null) {
            log.error("[create wedding] - user {} is already responsible for another wedding", responsible.getUsername());
            throw new BadRequestException(USER_ALREADY_RESPONSIBLE);
        }

        event = eventMapper.toEntity(weddingRequest);
        event.setResponsible(responsible);
        event.setEventType(responsible.getEventType());
        event = eventRepository.save(event);

        log.info("[create wedding] - end");
        return event.getUuid();

    }


    @Override
    @Transactional
    public void invite(String uuid, UserRequest request) {

        log.info("[invite] - start: {}", request.toString());

        Event event = getByUuid(uuid);
        if (event == null) {
            log.error("[invite] - wedding not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        if (Objects.nonNull(event.getMaxInvitations())) {
            int invitationsAlreadySent = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(event).size();
            if (event.getMaxInvitations() == invitationsAlreadySent) {
                log.error("[invite] - maximum number of invitations reached, uuid = {}", uuid);
                throw new BadRequestException(MAX_INVITATION_NUMBER_REACHED);
            }
        }

        if (Objects.nonNull(request.getTableNumber()) &&
                Objects.nonNull(event.getMaxTables()) &&
                (request.getTableNumber() > event.getMaxTables())) {
            log.error("[invite] - table number incorrect, uuid = {}", uuid);
            throw new BadRequestException(INCORRECT_TABLE_NUMBER);
        }

        request.setEventType(event.getEventType());
        request.setFirstName(StringUtils.capitalize(request.getFirstName()));
        request.setLastName(StringUtils.capitalize(request.getLastName()));
        User user = userService.save(request);

        invitationRepository.save(invitationMapper.toEntity(event, user, request));

        event.setTotalGuestsNotReplied(event.getTotalGuestsNotReplied() + request.getTotalInvitations());

        log.info("[invite] - save in authorization-server");
        AuthUserRequest authUserRequest = userMapper.toAuthUser(request);
        customUserDetailsService.addUserDetails(authUserRequest);

        log.info("[invite] - sending invitation mail...");
        sendMail(event, request);

        log.info("[invite] - end");
    }

    @Override
    public WeddingResponse getWedding(String uuid) {

        log.info("[get wedding] - start: uuid={}", uuid);

        WeddingResponse response = null;
        Event event = getByUuid(uuid);
        if (event != null) {
            List<Invitation> invitationList = invitationRepository.findByWedding(event);
            response = eventMapper.toModel(event, invitationList.size());
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
                String keyword = LIKE_KEYWORD_FORMAT.replace("keyword", Utils.toLowerCase(searchKeyword));
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get(GUEST_FIELD).get(USERNAME_FIELD), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(GUEST_FIELD).get(FIRSTNAME_FIELD)), keyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(GUEST_FIELD).get(LASTNAME_FIELD)), keyword)));
            }
            predicates.add(criteriaBuilder.isTrue(root.get(GUEST_FIELD).get(ENABLED_FIELD)));
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
        Page<Invitation> invitationsPage = invitationRepository.findAll(specification, pageable);

        ResponsePage<InvitationResponse> responsePage = new ResponsePage<>(invitationsPage.map(invitationMapper::toInvitation));
        if (!invitationsPage.isEmpty()) {
            Event event = invitationsPage.getContent().get(0).getEvent();
            responsePage.setTotalGuestsAttending(event.getTotalGuestsAttending());
            responsePage.setTotalGuestsNotAttending(event.getTotalGuestsNotAttending());
            responsePage.setTotalGuestsMaybe(event.getTotalGuestsMaybe());
            responsePage.setTotalGuestsNotReplied(event.getTotalGuestsNotReplied());
        }

        log.info("[get wedding invitations] - found {} element(s)", invitationsPage.getSize());
        log.info("[get wedding invitations] - end");

        return responsePage;
    }


    @Override
    public List<WeddingResponse> getWeddings(Pageable pageable) {

        log.info("[get weddings] - start");

        Page<Event> pageWeddings = eventRepository.findAll(pageable);
        List<WeddingResponse> response = pageWeddings
                .stream()
                .map(w -> eventMapper.toModel(w, invitationRepository.findByWedding(w).size()))
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

        Event event = eventRepository.findByUuid(uuid);
        if (event == null) {
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
        this.updateWedding(event, responsible, weddingUpdateRequest);

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

        Event event = getByUuid(uuidWedding);
        if (event == null) {
            event = eventRepository.findByUuid(uuidWedding);   // admin: delete (no matters date)
            if (event == null) {
                log.error("[delete wedding] - wedding not found, uuid = {}", uuidWedding);
                throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
            }
        }
        List<Invitation> invitationList = invitationRepository.findByWedding(event);
        invitationRepository.deleteAll(invitationList);   // delete all invitations
        invitationList.forEach(wg -> userService.deleteGuestInvitation(wg.getGuest().getUuid(), uuidWedding));  // delete guests
        userService.deleteGuestInvitation(event.getResponsible().getUuid(), uuidWedding);   // delete responsible
        eventRepository.delete(event);   // delete wedding

        log.info("[delete wedding] - end");

    }

    @Override
    public void updateInvitationStatus(UpdateInvitationRequest request, String uuidWedding, String uuidGuest) {

        log.info("[update invitation status] - start: uuidWedding={}, uuiGuest={}", uuidWedding, uuidGuest);

        Event event = getByUuid(uuidWedding);
        if (event == null) {
            log.error("[update invitation status] - wedding not found, uuid = {}", uuidWedding);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        User guest = userService.getByUuid(uuidGuest);
        if (guest == null) {
            log.error("[update invitation status] - user not found, uuid = {}", uuidGuest);
            throw new ResourceNotFoundException(USER_ALREADY_RESPONSIBLE);
        }

        Invitation invitation = invitationRepository.findByWeddingAndGuest(event, guest);
        if (invitation == null) {
            log.error("[update invitation status] - invitation not found, uuidWedding={}, uuiGuest={}", uuidWedding, uuidWedding);
            throw new ResourceNotFoundException(INVITATION_NOT_FOUND);
        }

        EventService.updateStatusInvitationNumber(event, invitation.getStatus(), invitation.getTotalInvitations(), "-");
        EventService.updateStatusInvitationNumber(event, request.getStatus(), invitation.getTotalInvitations(), "+");

        invitation.setStatus(request.getStatus());

        log.info("[update invitation status] - end");
    }

    /**
     * return pdf file that contains list of guests of a wedding
     */
    @Override
    public void downloadPdf(String uuidWedding, HttpServletResponse httpResponse) throws DocumentException, IOException {

        log.info("[download pdf] - start: uuidWedding={}", uuidWedding);

        Event event = getByUuid(uuidWedding);
        if (event == null) {
            log.error("[download pdf] - wedding not found, uuid = {}", uuidWedding);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        Document document = new Document();
        PdfWriter.getInstance(document, httpResponse.getOutputStream());
        document.open();
        PdfPTable table = new PdfPTable(3);
        if (event.getEventType() == WEDDING) {
            addTableTileAndHeaderWeddingEvent(table, event);
        } else {
            addTableTileAndHeader(table, event);
        }
        addRows(table, event);
        document.add(table);
        document.close();

        log.info("[download pdf] - end");

    }

    @Override
    public Event getByResponsible(User responsible) {
        Specification<Event> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("responsible").get("uuid"), responsible.getUuid()));
            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), new Date()));
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
        return eventRepository.findAll(specification).stream().findFirst().orElse(null);
    }

    @Override
    public Event getByUuid(String uuid) {
        return eventRepository.findByUuidAndDateGreaterThanEqual(uuid, new Date());
    }


    private void addRows(PdfPTable table, Event event) {
        List<Invitation> invitationList = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(event);
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

    private void addTableTileAndHeader(PdfPTable table, Event event) {
        // title
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        String title = INVITATION_PDF_MAIN_TITLE.formatted(event.getEventOwnerFirstName().toUpperCase(), event.getEventOwnerLastName().toUpperCase());
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(title, boldFont));
        pdfPCell.setColspan(4);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(20.0f);
        table.addCell(pdfPCell);

        List<String> columns = Arrays.asList(NOM, TABLE_NO, PRESENCE);

        // seats information
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        List<Invitation> invitationList = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(event);
        int seatsAvailable = event.getMaxInvitations() - invitationList.size();

        pdfPCell = new PdfPCell(new Paragraph(INVITATION_PDF_TITLE.formatted(event.getMaxInvitations(), invitationList.size(), seatsAvailable), font));
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

    private void addTableTileAndHeaderWeddingEvent(PdfPTable table, Event event) {
        // title
        Font boldFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
        String title = W_INVITATION_PDF_MAIN_TITLE.formatted(event.getHusbandName().toUpperCase(), event.getWifeName().toUpperCase());
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(title, boldFont));
        pdfPCell.setColspan(4);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(20.0f);
        table.addCell(pdfPCell);

        List<String> columns = Arrays.asList(NOM, TABLE_NO, PRESENCE);

        // seats information
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        List<Invitation> invitationList = invitationRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(event);
        int seatsAvailable = event.getMaxInvitations() - invitationList.size();

        pdfPCell = new PdfPCell(new Paragraph(INVITATION_PDF_TITLE.formatted(event.getMaxInvitations(), invitationList.size(), seatsAvailable), font));
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

    private void sendMail(Event event, UserRequest request) {
        try {
            String template = Utils.readFileFromResource(frInvitationBodyFilePath);
            if (Language.EN.equals(request.getLanguage())) {
                template = Utils.readFileFromResource(enInvitationBodyFilePath);
            }

            template = template.replace("{f_firstname}", event.getWifeName())
                    .replace("{m_firstname}", event.getHusbandName())
                    .replace("{login_uri}", uiLoginUri)
                    .replace("{username}", request.getUsername())
                    .replace("{password}", request.getPassword());

            // send to the guest
            emailSender.sendHtmlEmail(request.getEmail(), invitationSubject, template);
            // send a copy to the responsible for wedding
            emailSender.sendHtmlEmail(event.getResponsible().getEmail(), invitationSubject, template);
        } catch (Exception e) {
            log.error("[sendMail] - error sending email: {}", e.getMessage(), e);
        }

    }

    private void updateWedding(Event event, User responsible, WeddingUpdateRequest weddingRequest) {
        if (weddingRequest.getType() != null) event.setType(weddingRequest.getType());
        if (weddingRequest.getDate() != null) event.setDate(weddingRequest.getDate());
        if (weddingRequest.getDeadlineConfirmInvitation() != null)
            event.setDeadlineConfirmInvitation(weddingRequest.getDeadlineConfirmInvitation());
        if (StringUtils.isNotBlank(weddingRequest.getInvitationText()))
            event.setInvitationText(weddingRequest.getInvitationText());
        if (StringUtils.isNotBlank(weddingRequest.getInvitationOtherText()))
            event.setInvitationOtherText(weddingRequest.getInvitationOtherText());
        if (StringUtils.isNotBlank(weddingRequest.getWifeName())) event.setWifeName(weddingRequest.getWifeName());
        if (StringUtils.isNotBlank(weddingRequest.getHusbandName()))
            event.setHusbandName(weddingRequest.getHusbandName());
        if (StringUtils.isNotBlank(weddingRequest.getReceptionVenue()))
            event.setReceptionVenue(weddingRequest.getReceptionVenue());
        if (StringUtils.isNotBlank(weddingRequest.getGiftDetails()))
            event.setGiftDetails(weddingRequest.getGiftDetails());
        if (weddingRequest.getMaxInvitations() != null) event.setMaxInvitations(weddingRequest.getMaxInvitations());
        if (weddingRequest.getMaxTables() != null) event.setMaxTables(weddingRequest.getMaxTables());
        if (weddingRequest.getReceptionStartime() != null)
            event.setReceptionStartime(weddingRequest.getReceptionStartime());
        if (weddingRequest.getSpousesImage() != null) {
            event.setSpousesImage(weddingRequest.getSpousesImage().getBytes(StandardCharsets.UTF_8));
        }

        // others events
        if (weddingRequest.getEventName() != null) {
            event.setEventName(weddingRequest.getEventName());
        }
        if (weddingRequest.getEventOwnerLastName() != null) {
            event.setEventOwnerLastName(weddingRequest.getEventOwnerLastName());
        }
        if (weddingRequest.getEventOwnerFirstName() != null) {
            event.setEventOwnerFirstName(weddingRequest.getEventOwnerFirstName());
        }

        event.setCeremonyVenue(weddingRequest.getCeremonyVenue());
        event.setCeremonyStartime(weddingRequest.getCeremonyStartime());
        event.setResponsible(responsible);
    }

}
