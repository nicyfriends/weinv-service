package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.model.Event;
import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.dto.request.WeddingRequest;
import com.mainthreadlab.weinv.dto.response.WeddingResponse;
import com.mainthreadlab.weinv.commons.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static com.mainthreadlab.weinv.commons.Utils.isSourceDateBeforeTargetDate;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Value("${weinv.ui.default.invitation-other-text.fr}")
    private String defaultInvitationOtherTextFR;

    @Value("${weinv.ui.default.invitation-other-text.en}")
    private String defaultInvitationOtherTextEN;


    //@Mapping(target = "uuid", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "spousesImage", ignore = true)
    public abstract Event toEntity(WeddingRequest weddingRequest);

    @Mapping(target = "ceremonyStartime", ignore = true)
    @Mapping(target = "receptionStartime", ignore = true)
    @Mapping(target = "spousesImage", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "responsibleUUID", source = "wedding.responsible.uuid")
    public abstract WeddingResponse toModel(Event event, Integer numberOfSeatsTaken);

    @AfterMapping
    void setAfterMappingToEntity(WeddingRequest source, @MappingTarget Event target) {
        target.setUuid(UUID.randomUUID().toString());
        if (source.getSpousesImage() != null) {
            target.setSpousesImage(source.getSpousesImage().getBytes(StandardCharsets.UTF_8));
        }

        try {
            if (StringUtils.isBlank(source.getInvitationOtherText())) {
                String invitationOtherText = Utils.readFileFromResource(defaultInvitationOtherTextFR);
                if (Language.EN.equals(source.getLanguage())) {
                    invitationOtherText = Utils.readFileFromResource(defaultInvitationOtherTextEN);
                }
                target.setInvitationOtherText(invitationOtherText);
            }
        } catch (Exception ex) {
            log.error("[setAfterMappingToEntity] error: {}", ex.getMessage(), ex);
        }
    }

    @AfterMapping
    void setAfterMappingToModel(Event event, Integer numberOfSeatsTaken, @MappingTarget WeddingResponse target) {
        if (event.getCeremonyStartime() != null) {
            target.setCeremonyStartime(new SimpleDateFormat("HH:mm").format(event.getCeremonyStartime()));
        }
        target.setReceptionStartime(new SimpleDateFormat("HH:mm").format(event.getReceptionStartime()));
        if (event.getMaxInvitations() != null) {
            target.setInvitationsAvailable(event.getMaxInvitations() - numberOfSeatsTaken);
        }
        if (event.getSpousesImage() != null) {
            target.setSpousesImage(new String(event.getSpousesImage()));
        }
        target.setPrice(event.getResponsible().getPrice());
        target.setExpired(isSourceDateBeforeTargetDate(event.getDate(), new Date()));
        target.setResponsibleUsername(event.getResponsible().getUsername());

        if (event.getType() != null) {
            target.setType(event.getType().name());
        }
    }
}
