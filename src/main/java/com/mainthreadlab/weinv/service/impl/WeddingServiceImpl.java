package com.mainthreadlab.weinv.service.impl;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.dto.request.ConfirmRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.request.WeddingUpdateRequest;
import com.mainthreadlab.weinv.dto.response.InvitationResponse;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.enums.Language;
import com.mainthreadlab.weinv.enums.Role;
import com.mainthreadlab.weinv.exception.BadRequestException;
import com.mainthreadlab.weinv.exception.ResourceNotFoundException;
import com.mainthreadlab.weinv.mapper.UserMapper;
import com.mainthreadlab.weinv.mapper.WeddingGuestMapper;
import com.mainthreadlab.weinv.mapper.WeddingMapper;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.model.WeddingGuest;
import com.mainthreadlab.weinv.repository.WeddingGuestRepository;
import com.mainthreadlab.weinv.repository.WeddingRepository;
import com.mainthreadlab.weinv.service.EmailService;
import com.mainthreadlab.weinv.service.UserService;
import com.mainthreadlab.weinv.service.WeddingService;
import com.mainthreadlab.weinv.service.security.CustomUserDetailsService;
import com.mainthreadlab.weinv.util.CommonUtils;
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
import java.util.stream.Collectors;

import static com.mainthreadlab.weinv.enums.ErrorKey.*;
import static com.mainthreadlab.weinv.enums.EventType.WEDDING;
import static com.mainthreadlab.weinv.util.CommonUtils.isSourceDateBeforeTargetDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WeddingServiceImpl implements WeddingService {

    private final WeddingRepository weddingRepository;
    private final WeddingGuestRepository weddingGuestRepository;
    private final UserService userService;
    private final CustomUserDetailsService customUserDetailsService;
    private final EmailService emailSender;
    private final WeddingMapper weddingMapper;
    private final UserMapper userMapper;
    private final WeddingGuestMapper weddingGuestMapper;

    @Value("${weinv.ui.login-uri}")
    private String uiLoginUri;

    @Value("${weinv.mail.events.wedding.body.fr}")
    private String frInvitationBodyFilePath;

    @Value("${weinv.mail.events.wedding.body.en}")
    private String enInvitationBodyFilePath;

    @Value("${weinv.mail.events.wedding.subject}")
    private String invitationSubject;

    @Value("${weinv.client-id}")
    private String clientId;

    @Value("${weinv.client-secret}")
    private String clientSecret;

    @Value("${weinv.auth-server.users}")
    private String uriUsers;


    @Override
    public String createWedding(WeddingRequest weddingRequest) {

        log.info("[CreateWedding] - start: {}", weddingRequest.toString());

        if (weddingRequest.getDate() != null && isSourceDateBeforeTargetDate(weddingRequest.getDate(), new Date())) {
            log.error("[CreateWedding] - Wedding date must not be before the current date");
            throw new BadRequestException(INVALID_WEDDING_DATE);
        }

        weddingRequest.setHusbandName(StringUtils.capitalize(weddingRequest.getHusbandName()));
        weddingRequest.setWifeName(StringUtils.capitalize(weddingRequest.getWifeName()));

        User responsible = userService.getByUuid(weddingRequest.getResponsibleUUID());
        if (responsible == null) {
            log.error("[CreateWedding] - Wedding responsible not found, uuid = {}", weddingRequest.getResponsibleUUID());
            throw new ResourceNotFoundException(WEDDING_RESPONSIBLE_NOT_FOUND);
        }

        //control if the user is responsible for another wedding
        Wedding wedding = getByResponsible(responsible);
        if (wedding != null) {
            log.error("[CreateWedding] - User {} is already responsible for another wedding", responsible.getUsername());
            throw new BadRequestException(USER_ALREADY_RESPONSIBLE);
        }

        wedding = weddingMapper.toEntity(weddingRequest);
        wedding.setResponsible(responsible);
        wedding.setEventType(responsible.getEventType());
        wedding = weddingRepository.save(wedding);

        log.info("[CreateWedding] - success: uuid={}", wedding.getUuid());
        log.info("[CreateWedding] - end");
        return wedding.getUuid();

    }


    @Override
    @Transactional
    public void invite(String uuid, UserRequest userRequest) {

        log.info("[Invite] - start: {}", userRequest.toString());

        Wedding wedding = getByUuid(uuid);
        if (wedding == null) {
            log.error("[Invite] - Wedding not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        if (Objects.nonNull(wedding.getMaxInvitations())) {
            int invitationsAlreadySent = weddingGuestRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding).size();
            if (wedding.getMaxInvitations() == invitationsAlreadySent) {
                log.error("[Invite] - Maximum number of invitations reached, uuid = {}", uuid);
                throw new BadRequestException(MAX_INVITATION_NUMBER_REACHED);
            }
        }

        if (Objects.nonNull(userRequest.getTableNumber()) && Objects.nonNull(wedding.getMaxTables())) {
            if (userRequest.getTableNumber() > wedding.getMaxTables()) {
                log.error("[Invite] - Table number incorrect, uuid = {}", uuid);
                throw new BadRequestException(INCORRECT_TABLE_NUMBER);
            }
        }

        userRequest.setRole(Role.GUEST);
        userRequest.setEventType(wedding.getEventType());
        User user = userService.save(userRequest);

        weddingGuestRepository.save(weddingGuestMapper.toEntity(wedding, user, userRequest.getTableNumber()));

        log.info("[Invite] - save in authorization-server");
        AuthUserRequest authUserRequest = userMapper.toAuthUser(userRequest);
        customUserDetailsService.addUserDetails(authUserRequest);

        log.info("[Invite] - sending invitation mail...");
        sendMail(wedding, userRequest);

        log.info("[Invite] - success: username={}", userRequest.getUsername());
        log.info("[Invite] - end");

    }

    @Override
    public WeddingResponse getWedding(String uuid) {

        log.info("[GetWedding] - start: uuid={}", uuid);

        WeddingResponse response = null;
        Wedding wedding = getByUuid(uuid);
        if (wedding != null) {
            List<WeddingGuest> weddingGuestList = weddingGuestRepository.findByWedding(wedding);
            response = weddingMapper.toModel(wedding, weddingGuestList.size());
        }

        log.info("[GetWedding] - success: uuid={}", wedding != null ? wedding.getUuid() : null);
        log.info("[GetWedding] - end");
        return response;

    }

    // also search
    @Override
    public Page<InvitationResponse> getWeddingInvitations(String uuidWedding, String searchKeyword, Pageable pageable) {

        log.info("[GetWeddingInvitations] - start: uuidWedding={}", uuidWedding);

        Specification<WeddingGuest> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            query.orderBy(criteriaBuilder.asc(root.get("guest").get("firstName")));
            predicates.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("wedding").get("uuid"), uuidWedding),
                    criteriaBuilder.greaterThanOrEqualTo(root.get("wedding").get("date"), new Date())));

            if (StringUtils.isNotBlank(searchKeyword)) {
                String key = searchKeyword + "%";
                predicates.add(criteriaBuilder.or(criteriaBuilder.like(root.get("guest").get("username"), key),
                        criteriaBuilder.like(root.get("guest").get("firstName"), key),
                        criteriaBuilder.like(root.get("guest").get("lastName"), key)));
            }
            predicates.add(criteriaBuilder.isTrue(root.get("guest").get("enabled")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[]{}));
        };
        Page<WeddingGuest> weddingGuestList = weddingGuestRepository.findAll(specification, pageable);

        log.info("[GetWeddingInvitations] - success: {} element(s) found", weddingGuestList.getSize());
        log.info("[GetWeddingInvitations] - end");

        return weddingGuestList.map(weddingGuestMapper::toInvitation);

    }


    @Override
    public List<WeddingResponse> getWeddings(Pageable pageable) {

        log.info("[GetWeddings] - start");

        Page<Wedding> pageWeddings = weddingRepository.findAll(pageable);
        List<WeddingResponse> response = pageWeddings
                .stream()
                .map(w -> weddingMapper.toModel(w, weddingGuestRepository.findByWedding(w).size()))
                .collect(Collectors.toList());

        // remove unnecessary data (QoS)
        response.forEach(w -> w.setSpousesImage(null));

        log.info("[GetWeddings] - success: {} element(s) found", response.size());
        log.info("[GetWeddings] - end");
        return response;

    }

    @Override
    @Transactional
    public void updateWedding(String uuid, WeddingUpdateRequest weddingUpdateRequest, JwtDetails jwtDetails) {

        log.info("[UpdateWedding] - start: uuid={}", uuid);

        if (weddingUpdateRequest.getDate() != null && isSourceDateBeforeTargetDate(weddingUpdateRequest.getDate(), new Date())) {
            log.error("[UpdateWedding] - Wedding date must not be before the current date");
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
            log.error("[UpdateWedding] - Wedding not found, uuid = {}", uuid);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        User responsible = userService.getByUuid(weddingUpdateRequest.getResponsibleUUID());
        if (responsible == null) {
            log.error("[UpdateWedding] - Wedding responsible not found, uuid = {}", weddingUpdateRequest.getResponsibleUUID());
            throw new ResourceNotFoundException(WEDDING_RESPONSIBLE_NOT_FOUND);
        }

        this.updateWeddingPrice(weddingUpdateRequest, jwtDetails, responsible);
        this.responsiblePwdRecovery(weddingUpdateRequest, jwtDetails, responsible);
        this.updateWedding(wedding, responsible, weddingUpdateRequest);

        log.info("[UpdateWedding] - success: uuid={}", uuid);
        log.info("[UpdateWedding] - end");

    }

    private void updateWeddingPrice(WeddingUpdateRequest weddingRequest, JwtDetails jwtDetails, User responsible) {
        log.info("[updateWeddingPrice] - start");

        if (jwtDetails != null && jwtDetails.getAuthorities() != null && !jwtDetails.getAuthorities().isEmpty()) {
            List<String> authorities = jwtDetails.getAuthorities();
            if (authorities.contains("admin") && weddingRequest.getPrice() != null) {
                responsible.setPrice(weddingRequest.getPrice());
            }
        }
        log.info("[updateWeddingPrice] - end");
    }

    private void responsiblePwdRecovery(WeddingUpdateRequest weddingRequest, JwtDetails jwtDetails, User responsible) {
        log.info("[responsiblePwdRecovery] - start");

        if (jwtDetails != null && jwtDetails.getAuthorities() != null && !jwtDetails.getAuthorities().isEmpty()) {
            List<String> authorities = jwtDetails.getAuthorities();
            if (authorities.contains("admin") && StringUtils.isNotBlank(weddingRequest.getResponsibleNewPassword())) {
                customUserDetailsService.responsiblePwdRecovery(responsible.getUsername(), weddingRequest.getResponsibleNewPassword());
            }
        }
        log.info("[responsiblePwdRecovery] - end");
    }

    @Override
    public void deleteWedding(String uuidWedding) {
        log.info("[DeleteWedding] - start: uuid={}", uuidWedding);

        Wedding wedding = getByUuid(uuidWedding);
        if (wedding == null) {
            wedding = weddingRepository.findByUuid(uuidWedding);   // admin: delete (no matters date)
            if (wedding == null) {
                log.error("[DeleteWedding] - Wedding not found, uuid = {}", uuidWedding);
                throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
            }
        }
        List<WeddingGuest> weddingGuestList = weddingGuestRepository.findByWedding(wedding);
        weddingGuestRepository.deleteAll(weddingGuestList);   // delete all invitations
        weddingGuestList.forEach(wg -> userService.deleteUser(wg.getGuest().getUuid(), uuidWedding));  // delete guests
        userService.deleteUser(wedding.getResponsible().getUuid(), uuidWedding);   // delete responsible
        weddingRepository.delete(wedding);   // delete wedding

        log.info("[DeleteWedding] - success: uuid={}", uuidWedding);
        log.info("[DeleteWedding] - end");

    }

    @Override
    public void confirmInvitation(ConfirmRequest confirmRequest, String uuidWedding, String uuidGuest) {

        log.info("[ConfirmInvitation] - start: uuidWedding={}, uuiGuest={}", uuidWedding, uuidGuest);

        Wedding wedding = getByUuid(uuidWedding);
        if (wedding == null) {
            log.error("[ConfirmInvitation] - Wedding not found, uuid = {}", uuidWedding);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        User guest = userService.getByUuid(uuidGuest);
        if (guest == null) {
            log.error("[ConfirmInvitation] - User not found, uuid = {}", uuidGuest);
            throw new ResourceNotFoundException(USER_ALREADY_RESPONSIBLE);
        }

        WeddingGuest weddingGuest = weddingGuestRepository.findByWeddingAndGuest(wedding, guest);
        if (weddingGuest == null) {
            log.error("[ConfirmInvitation] - Invitation not found, uuidWedding={}, uuiGuest={}", uuidWedding, uuidWedding);
            throw new ResourceNotFoundException(INVITATION_NOT_FOUND);
        }

        if (confirmRequest.getAccept() != null && confirmRequest.getAccept()) {
            weddingGuest.setConfirmed(true);
            weddingGuest.setRejected(false);
        } else {
            weddingGuest.setRejected(true);
            weddingGuest.setConfirmed(false);
        }

        log.info("[ConfirmInvitation] - success");
        log.info("[ConfirmInvitation] - end");

    }

    /**
     * return pdf file that contains list of guests of a wedding
     *
     * @return
     */
    @Override
    public void downloadPdf(String uuidWedding, HttpServletResponse httpResponse) throws DocumentException, IOException {

        log.info("[DownloadPdf] - start: uuidWedding={}", uuidWedding);

        Wedding wedding = getByUuid(uuidWedding);
        if (wedding == null) {
            log.error("[ConfirmInvitation] - Wedding not found, uuid = {}", uuidWedding);
            throw new ResourceNotFoundException(WEDDING_NOT_FOUND);
        }

        Document document = new Document();
        PdfWriter.getInstance(document, httpResponse.getOutputStream());
        document.open();
        PdfPTable table = new PdfPTable(4);
        if (wedding.getEventType() == WEDDING) {
            addTableTileAndHeaderWeddingEvent(table, wedding);
        } else {
            addTableTileAndHeader(table, wedding);
        }
        addRows(table, wedding);
        document.add(table);
        document.close();

        log.info("[DownloadPdf] - success");
        log.info("[DownloadPdf] - end");

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
        List<WeddingGuest> weddingGuestList = weddingGuestRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding);
        for (WeddingGuest wg : weddingGuestList) {
            table.addCell(getRowPdfPCell(wg.getGuest().getFirstName() + " " + wg.getGuest().getLastName()));
            table.addCell(getRowPdfPCell(wg.getTableNumber() != null ? wg.getTableNumber().toString() : null));
            table.addCell(getRowPdfPCell(wg.isConfirmed() ? "X" : ""));
            table.addCell(getRowPdfPCell(wg.isRejected() ? "X" : ""));
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
        String title = "ANNIVERSAIRE DE " + wedding.getEventOwnerFirstName().toUpperCase() + " " + wedding.getEventOwnerLastName().toUpperCase();
        PdfPCell pdfPCell = new PdfPCell(new Paragraph(title, boldFont));
        pdfPCell.setColspan(4);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(20.0f);
        table.addCell(pdfPCell);

        List<String> columns = Arrays.asList("Nom", "Table No", "Confirmé", "Rejeté");
        String maxSeatsTitle = "Nombre d'invitations: ";
        String seatsTaken = "Invitations envoyées: ";
        String seatsAvailableTitle = "Invitations disponibiles: ";
        String maxTables = "Nombre de tables: ";

        if (Language.EN.equals(wedding.getResponsible().getLanguage())) {
            maxSeatsTitle = "Number of invitations: ";
            maxTables = "Number of tables: ";
            seatsTaken = "Invitations sent: ";
            seatsAvailableTitle = "Invitations available: ";
            columns = Arrays.asList("Name", "Table No", "Confirmed", "Rejected");
        }

        // seats information
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        List<WeddingGuest> weddingGuestList = weddingGuestRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding);
        int seatsAvailable = wedding.getMaxInvitations() - weddingGuestList.size();
        String infos = maxTables + wedding.getMaxTables() + "\n"
                + maxSeatsTitle + wedding.getMaxInvitations() + "\n"
                + seatsTaken + weddingGuestList.size() + "\n"
                + seatsAvailableTitle + seatsAvailable;
        pdfPCell = new PdfPCell(new Paragraph(infos, font));
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

        String title;
        if (wedding.getEventType() == WEDDING) {
            title = "MARIAGE DE " + wedding.getHusbandName().toUpperCase() + " ET " + wedding.getWifeName().toUpperCase();
        } else {
            title = "ANNIVERSAIRE DE " + wedding.getEventOwnerFirstName().toUpperCase() + " " + wedding.getEventOwnerLastName().toUpperCase();
        }

        PdfPCell pdfPCell = new PdfPCell(new Paragraph(title, boldFont));
        pdfPCell.setColspan(4);
        pdfPCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        pdfPCell.setPadding(20.0f);
        table.addCell(pdfPCell);

        List<String> columns = Arrays.asList("Nom", "Table No", "Confirmé", "Rejeté");
        String maxSeatsTitle = "Nombre d'invitations: ";
        String seatsTaken = "Invitations envoyées: ";
        String seatsAvailableTitle = "Invitations disponibiles: ";
        String maxTables = "Nombre de tables: ";

        if (Language.EN.equals(wedding.getResponsible().getLanguage())) {
            maxSeatsTitle = "Number of invitations: ";
            maxTables = "Number of tables: ";
            seatsTaken = "Invitations sent: ";
            seatsAvailableTitle = "Invitations available: ";
            columns = Arrays.asList("Name", "Table No", "Confirmed", "Rejected");
        }

        // seats information
        Font font = new Font(Font.FontFamily.TIMES_ROMAN, 14);
        List<WeddingGuest> weddingGuestList = weddingGuestRepository.findByWeddingOrderByGuest_FirstNameAscGuest_LastNameAsc(wedding);
        int seatsAvailable = wedding.getMaxInvitations() - weddingGuestList.size();
        String infos = maxTables + wedding.getMaxTables() + "\n"
                + maxSeatsTitle + wedding.getMaxInvitations() + "\n"
                + seatsTaken + weddingGuestList.size() + "\n"
                + seatsAvailableTitle + seatsAvailable;
        pdfPCell = new PdfPCell(new Paragraph(infos, font));
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

    private void sendMail(Wedding wedding, UserRequest userRequest) {
        try {
            String template = CommonUtils.readFileFromResource(frInvitationBodyFilePath);
            if (Language.EN.equals(userRequest.getLanguage())) {
                template = CommonUtils.readFileFromResource(enInvitationBodyFilePath);
            }

            template = template.replace("{f_firstname}", wedding.getWifeName())
                    .replace("{m_firstname}", wedding.getHusbandName())
                    .replace("{login_uri}", uiLoginUri)
                    .replace("{username}", userRequest.getUsername())
                    .replace("{password}", userRequest.getPassword());

            // send to the guest
            emailSender.sendHtmlEmail(userRequest.getEmail(), invitationSubject, template);
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
