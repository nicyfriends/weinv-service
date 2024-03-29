package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.commons.Utils;
import com.mainthreadlab.weinv.dto.request.EventRequest;
import com.mainthreadlab.weinv.dto.response.EventResponse;
import com.mainthreadlab.weinv.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static com.mainthreadlab.weinv.commons.Utils.isSourceDateBeforeTargetDate;

@Slf4j
@Mapper(componentModel = "spring")
public abstract class EventMapper {

    @Value("${weinv.ui.default.invitation-other-text.fr}")
    private String defaultInvitationOtherTextFR;


    //@Mapping(target = "uuid", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "spousesImage", ignore = true)
    public abstract Event toEntity(EventRequest eventRequest);

    @Mapping(target = "ceremonyStartime", ignore = true)
    @Mapping(target = "receptionStartime", ignore = true)
    @Mapping(target = "spousesImage", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "responsibleUUID", source = "event.responsible.uuid")
    public abstract EventResponse toModel(Event event, Integer numberOfSeatsTaken);

    @AfterMapping
    void setAfterMappingToEntity(EventRequest source, @MappingTarget Event target) {
        target.setUuid(UUID.randomUUID().toString());
        if (source.getSpousesImage() != null) {
            target.setSpousesImage(source.getSpousesImage().getBytes(StandardCharsets.UTF_8));
        }

        try {
            if (StringUtils.isBlank(source.getInvitationOtherText())) {
                String invitationOtherText = Utils.readFileFromResource(defaultInvitationOtherTextFR);
                target.setInvitationOtherText(invitationOtherText);
            }
        } catch (Exception ex) {
            log.error("[set after mapping to entity] error: {}", ex.getMessage(), ex);
        }
    }

    @AfterMapping
    void setAfterMappingToModel(Event event, Integer numberOfSeatsTaken, @MappingTarget EventResponse target) {
        if (event.getCeremonyStartime() != null) {
            target.setCeremonyStartime(new SimpleDateFormat("HH:mm").format(event.getCeremonyStartime()));
        }
        target.setReceptionStartime(new SimpleDateFormat("HH:mm").format(event.getReceptionStartime()));
        if (event.getMaxInvitations() != null) {
            target.setInvitationsAvailable(event.getMaxInvitations() - numberOfSeatsTaken);
        }
        /** make call response faster*/
//        if (event.getSpousesImage() != null) {
//            target.setSpousesImage(new String(event.getSpousesImage()));
//        }
        target.setPrice(event.getResponsible().getPrice());
        target.setExpired(isSourceDateBeforeTargetDate(event.getDate(), new Date()));
        target.setResponsibleUsername(event.getResponsible().getUsername());

        if (event.getType() != null) {
            target.setType(event.getType().name());
        }
    }
}
