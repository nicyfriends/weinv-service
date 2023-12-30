package com.mainthreadlab.weinv.dto.request;

import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.enums.WeddingType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class WeddingRequest {

    //@NotNull(message = "must not be empty or null")
    private WeddingType type;

    @NotBlank(message = "must not be empty or null")
    private String invitationText;

    private String invitationOtherText;

    //@NotBlank(message = "must not be empty or null")
    private String wifeName;

    //@NotBlank(message = "must not be empty or null")
    private String husbandName;

    @NotNull(message = "must not be empty or null")
    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.DATE)
    private Date deadlineConfirmInvitation;

    @Temporal(TemporalType.TIME)
    private Date ceremonyStartime;

    private String ceremonyVenue;      // "venue name, 12, street name, comune/zipcode, city"

    @NotNull(message = "must not be empty or null")
    @Temporal(TemporalType.TIME)
    private Date receptionStartime;

    @NotBlank(message = "must not be empty or null")
    private String receptionVenue;

    private Integer maxReceptionSeats;

    private Integer maxTables;

    @NotBlank(message = "must not be empty or null")
    private String responsibleUUID;                          // it would be a person with 'user' role

    private Language language;

    private String spousesImage;   // img in base64 format

    private String giftDetails;

    // others events
    private String eventOwnerFirstName;
    private String eventOwnerLastName;
    private String eventName;

}
