package com.mainthreadlab.weinv.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mainthreadlab.weinv.commons.Utils;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.dto.request.UpdateInvitationRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.EventRequest;
import com.mainthreadlab.weinv.dto.request.EventUpdateRequest;
import com.mainthreadlab.weinv.dto.response.EventResponse;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.ResponsePage;
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

    @Value("${weinv.mail.events.event.body.fr}")
    private String frInvitationBodyFilePath;

    @Value("${weinv.mail.events.event.subject}")
    private String invitationSubject;


    @Override
    public String createEvent(EventRequest eventRequest) {

        log.info("[create event] - start: {}", eventRequest.toString());

        if (eventRequest.getDate() != null && isSourceDateBeforeTargetDate(eventRequest.getDate(), new Date())) {
            log.error("[create event] - event date must not be before the current date");
            throw new BadRequestException(INVALID_EVENT_DATE);
        }

        eventRequest.setHusbandName(StringUtils.capitalize(eventRequest.getHusbandName()));
        eventRequest.setWifeName(StringUtils.capitalize(eventRequest.getWifeName()));

        User responsible = userService.getByUuid(eventRequest.getResponsibleUUID());
        if (responsible == null) {
            log.error("[create event] - event responsible not found, uuid = {}", eventRequest.getResponsibleUUID());
            throw new ResourceNotFoundException(EVENT_RESPONSIBLE_NOT_FOUND);
        }

        //control if the user is responsible for another event
        Event event = getByResponsible(responsible);
        if (event != null) {
            log.error("[create event] - user {} is already responsible for another event", responsible.getUsername());
            throw new BadRequestException(USER_ALREADY_RESPONSIBLE);
        }

        event = eventMapper.toEntity(eventRequest);
        event.setResponsible(responsible);
        event.setEventType(responsible.getEventType());
        event = eventRepository.save(event);

        log.info("[create event] - end");
        return event.getUuid();

    }


    @Override
    @Transactional
    public void invite(String uuid, UserRequest request) {

        log.info("[invite] - start: {}", request.toString());

        Event event = getByUuid(uuid);
        if (event == null) {
            log.error("[invite] - event not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(EVENT_NOT_FOUND);
        }

        if (Objects.nonNull(event.getMaxInvitations())) {
            int invitationsAlreadySent = invitationRepository.findByEventOrderByGuest_FirstNameAscGuest_LastNameAsc(event).size();
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
    public EventResponse getEvent(String uuid) {

        log.info("[get event] - start: uuid={}", uuid);

        EventResponse response = null;
        Event event = getByUuid(uuid);
        if (event != null) {
            List<Invitation> invitationList = invitationRepository.findByEvent(event);
            response = eventMapper.toModel(event, invitationList.size());
        }

        log.info("[get event] - end");
        return response;
    }

    // also search
    @Override
    public ResponsePage<InvitationResponse> getEventInvitations(String uuidEvent, String searchKeyword, Pageable pageable, InvitationStatus invitationStatus) {

        log.info("[get event invitations] - start: uuidEvent={}", uuidEvent);

        Specification<Invitation> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.orderBy(criteriaBuilder.asc(root.get(GUEST_FIELD).get(FIRSTNAME_FIELD)));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get(EVENT_FIELD).get(UUID_FIELD), uuidEvent),
                    criteriaBuilder.greaterThanOrEqualTo(root.get(EVENT_FIELD).get(EVENT_DATE_FIELD), new Date())));

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

        log.info("[get event invitations] - found {} element(s)", invitationsPage.getSize());
        log.info("[get event invitations] - end");

        return responsePage;
    }


    @Override
    public List<EventResponse> getEvents(Pageable pageable) {
        log.info("[get events] - start");

        Page<Event> page = eventRepository.findAll(pageable);
        List<EventResponse> response = page
                .stream()
                .map(w -> eventMapper.toModel(w, invitationRepository.findByEvent(w).size()))
                .toList();

        // remove unnecessary data (QoS)
        response.forEach(w -> w.setSpousesImage(null));

        log.info("[get events] - found {} element(s)", response.size());
        log.info("[get events] - end");
        return response;
    }

    @Override
    @Transactional
    public void updateEvent(String uuid, EventUpdateRequest eventUpdateRequest, JwtDetails jwtDetails) {

        log.info("[update event] - start: uuid={}", uuid);

        if (eventUpdateRequest.getDate() != null && isSourceDateBeforeTargetDate(eventUpdateRequest.getDate(), new Date())) {
            log.error("[update event] - event date must not be before the current date");
            throw new BadRequestException(INVALID_EVENT_DATE);
        }

        if (StringUtils.isNotBlank(eventUpdateRequest.getHusbandName())) {
            eventUpdateRequest.setHusbandName(StringUtils.capitalize(eventUpdateRequest.getHusbandName()));
        }
        if (StringUtils.isNotBlank(eventUpdateRequest.getWifeName())) {
            eventUpdateRequest.setWifeName(StringUtils.capitalize(eventUpdateRequest.getWifeName()));
        }

        Event event = eventRepository.findByUuid(uuid);
        if (event == null) {
            log.error("[update event] - event not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(EVENT_NOT_FOUND);
        }

        User responsible = userService.getByUuid(eventUpdateRequest.getResponsibleUUID());
        if (responsible == null) {
            log.error("[update event] - event responsible not found, uuid = {}", eventUpdateRequest.getResponsibleUUID());
            throw new ResourceNotFoundException(EVENT_RESPONSIBLE_NOT_FOUND);
        }

        this.updateEventPrice(eventUpdateRequest, jwtDetails, responsible);
        this.responsiblePwdRecovery(eventUpdateRequest, jwtDetails, responsible);
        this.updateEvent(event, responsible, eventUpdateRequest);

        log.info("[update event] - end");

    }

    private void updateEventPrice(EventUpdateRequest request, JwtDetails jwtDetails, User responsible) {
        log.info("[update event price] - start");

        if (jwtDetails != null && jwtDetails.getAuthorities() != null && !jwtDetails.getAuthorities().isEmpty()) {
            List<String> authorities = jwtDetails.getAuthorities();
            if (authorities.contains("admin") && request.getPrice() != null) {
                responsible.setPrice(request.getPrice());
            }
        }
        log.info("[update event price] - end");
    }

    private void responsiblePwdRecovery(EventUpdateRequest request, JwtDetails jwtDetails, User responsible) {
        log.info("[responsible password recovery] - start");

        if (jwtDetails != null && jwtDetails.getAuthorities() != null && !jwtDetails.getAuthorities().isEmpty()) {
            List<String> authorities = jwtDetails.getAuthorities();
            if (authorities.contains("admin") && StringUtils.isNotBlank(request.getResponsibleNewPassword())) {
                customUserDetailsService.responsiblePwdRecovery(responsible.getUsername(), request.getResponsibleNewPassword());
            }
        }
        log.info("[responsible password recovery] - end");
    }

    @Override
    public void deleteEvent(String uuidEvent) {
        log.info("[delete event] - start: uuid={}", uuidEvent);

        Event event = getByUuid(uuidEvent);
        if (event == null) {
            event = eventRepository.findByUuid(uuidEvent);   // admin: delete (no matters date)
            if (event == null) {
                log.error("[delete event] - event not found, uuid = {}", uuidEvent);
                throw new ResourceNotFoundException(EVENT_NOT_FOUND);
            }
        }
        List<Invitation> invitationList = invitationRepository.findByEvent(event);
        invitationRepository.deleteAll(invitationList);   // delete all invitations
        invitationList.forEach(wg -> userService.deleteGuestInvitation(wg.getGuest().getUuid(), uuidEvent));  // delete guests
        userService.deleteGuestInvitation(event.getResponsible().getUuid(), uuidEvent);   // delete responsible
        eventRepository.delete(event);   // delete event

        log.info("[delete event] - end");

    }

    @Override
    public void updateInvitationStatus(UpdateInvitationRequest request, String uuidEvent, String uuidGuest) {

        log.info("[update invitation status] - start: uuidEvent={}, uuiGuest={}", uuidEvent, uuidGuest);

        Event event = getByUuid(uuidEvent);
        if (event == null) {
            log.error("[update invitation status] - event not found, uuid = {}", uuidEvent);
            throw new ResourceNotFoundException(EVENT_NOT_FOUND);
        }

        User guest = userService.getByUuid(uuidGuest);
        if (guest == null) {
            log.error("[update invitation status] - user not found, uuid = {}", uuidGuest);
            throw new ResourceNotFoundException(USER_ALREADY_RESPONSIBLE);
        }

        Invitation invitation = invitationRepository.findByEventAndGuest(event, guest);
        if (invitation == null) {
            log.error("[update invitation status] - invitation not found, uuidEvent={}, uuiGuest={}", uuidEvent, uuidEvent);
            throw new ResourceNotFoundException(INVITATION_NOT_FOUND);
        }

        EventService.updateStatusInvitationNumber(event, invitation.getStatus(), invitation.getTotalInvitations(), "-");
        EventService.updateStatusInvitationNumber(event, request.getStatus(), invitation.getTotalInvitations(), "+");

        invitation.setStatus(request.getStatus());

        log.info("[update invitation status] - end");
    }

    /**
     * return pdf file that contains list of guests of an event
     */
    @Override
    public void downloadPdf(String uuidEvent, HttpServletResponse httpResponse) throws DocumentException, IOException {

        log.info("[download pdf] - start: uuidEvent={}", uuidEvent);

        Event event = getByUuid(uuidEvent);
        if (event == null) {
            log.error("[download pdf] - event not found, uuid = {}", uuidEvent);
            throw new ResourceNotFoundException(EVENT_NOT_FOUND);
        }

        Document document = new Document();
        PdfWriter.getInstance(document, httpResponse.getOutputStream());
        document.open();
        PdfPTable table = new PdfPTable(3);
        if (event.getEventType() == WEDDING) {
            addTableTileAndHeaderWeddingEvent(table, event);
        } else {
            addTableTileAndHeaderBirthdayEvent(table, event);
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
        List<Invitation> invitationList = invitationRepository.findByEventOrderByGuest_FirstNameAscGuest_LastNameAsc(event);
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

    private void addTableTileAndHeaderBirthdayEvent(PdfPTable table, Event event) {
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
        List<Invitation> invitationList = invitationRepository.findByEventOrderByGuest_FirstNameAscGuest_LastNameAsc(event);
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
        List<Invitation> invitationList = invitationRepository.findByEventOrderByGuest_FirstNameAscGuest_LastNameAsc(event);
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
            String template = Utils.readFileFromResource(frInvitationBodyFilePath)
                    .replace("{f_firstname}", event.getWifeName())
                    .replace("{m_firstname}", event.getHusbandName())
                    .replace("{login_uri}", uiLoginUri)
                    .replace("{username}", request.getUsername())
                    .replace("{password}", request.getPassword());

            // send to the guest
            emailSender.sendHtmlEmail(request.getEmail(), invitationSubject, template);
            // send a copy to the responsible for event
            emailSender.sendHtmlEmail(event.getResponsible().getEmail(), invitationSubject, template);
        } catch (Exception e) {
            log.error("[sendMail] - error sending email: {}", e.getMessage(), e);
        }

    }

    private void updateEvent(Event event, User responsible, EventUpdateRequest request) {
        if (request.getType() != null) event.setType(request.getType());
        if (request.getDate() != null) event.setDate(request.getDate());
        if (request.getDeadlineConfirmInvitation() != null)
            event.setDeadlineConfirmInvitation(request.getDeadlineConfirmInvitation());
        if (StringUtils.isNotBlank(request.getInvitationText()))
            event.setInvitationText(request.getInvitationText());
        if (StringUtils.isNotBlank(request.getInvitationOtherText()))
            event.setInvitationOtherText(request.getInvitationOtherText());
        if (StringUtils.isNotBlank(request.getWifeName())) event.setWifeName(request.getWifeName());
        if (StringUtils.isNotBlank(request.getHusbandName()))
            event.setHusbandName(request.getHusbandName());
        if (StringUtils.isNotBlank(request.getReceptionVenue()))
            event.setReceptionVenue(request.getReceptionVenue());
        if (StringUtils.isNotBlank(request.getGiftDetails()))
            event.setGiftDetails(request.getGiftDetails());
        if (request.getMaxInvitations() != null) event.setMaxInvitations(request.getMaxInvitations());
        if (request.getMaxTables() != null) event.setMaxTables(request.getMaxTables());
        if (request.getReceptionStartime() != null)
            event.setReceptionStartime(request.getReceptionStartime());
        if (request.getSpousesImage() != null) {
            event.setSpousesImage(request.getSpousesImage().getBytes(StandardCharsets.UTF_8));
        }

        // others events
        if (request.getEventName() != null) {
            event.setEventName(request.getEventName());
        }
        if (request.getEventOwnerLastName() != null) {
            event.setEventOwnerLastName(request.getEventOwnerLastName());
        }
        if (request.getEventOwnerFirstName() != null) {
            event.setEventOwnerFirstName(request.getEventOwnerFirstName());
        }

        event.setCeremonyVenue(request.getCeremonyVenue());
        event.setCeremonyStartime(request.getCeremonyStartime());
        event.setResponsible(responsible);
    }

}
