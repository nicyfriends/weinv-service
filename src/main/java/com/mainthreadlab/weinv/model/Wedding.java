package com.mainthreadlab.weinv.model;

import com.mainthreadlab.weinv.model.enums.EventType;
import com.mainthreadlab.weinv.model.enums.WeddingType;
import com.mainthreadlab.weinv.model.base.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@Table(name = "ws_weddings")
public class Wedding extends BaseEntity {

    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wedding_seq")
//    @SequenceGenerator(name = "wedding_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String uuid;

    //@Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WeddingType type;

    //@Column(nullable = false)
    private String wifeName;

    //@Column(nullable = false)
    private String husbandName;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date date;

    @Temporal(TemporalType.TIME)
    private Date ceremonyStartime;

    private String ceremonyVenue;      // "venue name, 12, street name, commune/zipcode, city"

    @Temporal(TemporalType.TIME)
    @Column(nullable = false)
    private Date receptionStartime;

    @Column(nullable = false)
    private String receptionVenue;

    private Integer maxInvitations;

    private Integer maxTables;

    @Temporal(TemporalType.DATE)
    private Date deadlineConfirmInvitation;

    // it would be a person with 'user' role
    @ToString.Exclude
    @OneToOne
    @JoinColumn(name = "responsible", referencedColumnName = "uuid", foreignKey = @ForeignKey(name = "fk_wedding_user"))
    private User responsible;

    @Column(name = "invitation_text", length = 3000)
    private String invitationText;

    @Column(name = "invitation_other_text", length = 3000)
    private String invitationOtherText;

    @Column(name = "gift_details")
    private String giftDetails;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "spouses_image")
    private byte[] spousesImage;

    @Column(name = "url_video")
    private String urlVideo;   //nic & evo youtube video (special case)

    // others events
    private String eventOwnerFirstName;
    private String eventOwnerLastName;
    private String eventName;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    private int totalGuestsAttending;
    private int totalGuestsNotAttending;
    private int totalGuestsMaybe;
    private int totalGuestsNotReplied;

    //@Lob >> remember that I am not using it anymore to avoid the exception on the browser
    @Column(length = 16000000) // This should generate a medium blob
    @Basic(fetch = FetchType.LAZY) // I've read this is default, but anyway...
    public byte[] getSpousesImage() {
        return spousesImage;
    }

    public void setSpousesImage(byte[] spousesImage) {
        this.spousesImage = spousesImage;
    }


}