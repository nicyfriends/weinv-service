package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.model.Wedding;
import com.mainthreadlab.weinv.enums.Language;
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
public abstract class WeddingMapper {

    @Value("${weinv.ui.default.invitation-other-text.fr}")
    private String defaultInvitationOtherTextFR;

    @Value("${weinv.ui.default.invitation-other-text.en}")
    private String defaultInvitationOtherTextEN;


    //@Mapping(target = "uuid", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "responsible", ignore = true)
    @Mapping(target = "spousesImage", ignore = true)
    public abstract Wedding toEntity(WeddingRequest weddingRequest);

    @Mapping(target = "ceremonyStartime", ignore = true)
    @Mapping(target = "receptionStartime", ignore = true)
    @Mapping(target = "spousesImage", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "responsibleUUID", source = "wedding.responsible.uuid")
    public abstract WeddingResponse toModel(Wedding wedding, Integer numberOfSeatsTaken);

    @AfterMapping
    void setAfterMappingToEntity(WeddingRequest source, @MappingTarget Wedding target) {
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
    void setAfterMappingToModel(Wedding wedding, Integer numberOfSeatsTaken, @MappingTarget WeddingResponse target) {
        if (wedding.getCeremonyStartime() != null) {
            target.setCeremonyStartime(new SimpleDateFormat("HH:mm").format(wedding.getCeremonyStartime()));
        }
        target.setReceptionStartime(new SimpleDateFormat("HH:mm").format(wedding.getReceptionStartime()));
        if (wedding.getMaxInvitations() != null) {
            target.setInvitationsAvailable(wedding.getMaxInvitations() - numberOfSeatsTaken);
        }
        if (wedding.getSpousesImage() != null) {
            target.setSpousesImage(new String(wedding.getSpousesImage()));
        }
        target.setPrice(wedding.getResponsible().getPrice());
        target.setExpired(isSourceDateBeforeTargetDate(wedding.getDate(), new Date()));
        target.setResponsibleUsername(wedding.getResponsible().getUsername());

        if (wedding.getType() != null) {
            target.setType(wedding.getType().name());
        }
    }
}
