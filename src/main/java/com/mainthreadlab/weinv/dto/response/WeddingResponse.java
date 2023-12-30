package com.mainthreadlab.weinv.dto.response;

import com.mainthreadlab.weinv.model.enums.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class WeddingResponse {

    private String uuid;

    private String type;

    private Integer invitationsAvailable;

    private Integer maxInvitations;

    private Integer maxTables;

    private Date date;

    private String wifeName;

    private String husbandName;

    private String ceremonyStartime;   // hh:mm

    private String ceremonyVenue;      // "venue name, 12, street name, comune/zipcode, city"

    private String receptionStartime;     // hh:mm

    private String receptionVenue;

    private Date deadlineConfirmInvitation;

    private String invitationText;

    private String invitationOtherText;

    private String responsibleUUID;

    private String responsibleUsername;

    private String spousesImage;

    private String urlVideo;   //nic & evo youtube video (special case)

    private String giftDetails;

    private Double price;

    // others events
    private String eventOwnerFirstName;
    private String eventOwnerLastName;
    private String eventName;
    private EventType eventType;

    private boolean expired;

}
