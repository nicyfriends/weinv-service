package com.mainthreadlab.weinv.dto.request;

import com.mainthreadlab.weinv.model.enums.Language;
import com.mainthreadlab.weinv.model.enums.WeddingCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class EventUpdateRequest {

    private WeddingCategory type;

    private String invitationText;
    private String invitationOtherText;
    private String wifeName;
    private String husbandName;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Temporal(TemporalType.DATE)
    private Date deadlineConfirmInvitation;

    @Temporal(TemporalType.TIME)
    private Date ceremonyStartime;

    private String ceremonyVenue;      // "venue name, 12, street name, comune/zipcode, city"

    @Temporal(TemporalType.TIME)
    private Date receptionStartime;

    private String receptionVenue;
    private Integer maxInvitations;
    private Integer maxTables;

    @NotBlank(message = "must not be empty or null")
    private String responsibleUUID;                          // it would be a person with 'user' role

    private Language language;
    private String spousesImage;   // img in base64 format
    private String giftDetails;
    private Double price;
    private String responsibleNewPassword;    // change made by admin (in case of forgot pwd)


    // others events
    private String eventOwnerFirstName;
    private String eventOwnerLastName;
    private String eventName;

}
