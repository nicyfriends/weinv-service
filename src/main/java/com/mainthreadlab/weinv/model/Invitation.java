package com.mainthreadlab.weinv.model;

import com.mainthreadlab.weinv.model.base.BaseEntity;
import com.mainthreadlab.weinv.model.base.InvitationId;
import com.mainthreadlab.weinv.model.enums.InvitationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Setter
@Getter
@ToString
@NoArgsConstructor
@Table(name = "ws_invitations")
public class Invitation extends BaseEntity {

    @EmbeddedId
    private InvitationId id;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "wedding_uuid", referencedColumnName = "uuid", foreignKey = @ForeignKey(name = "fk_wedding_guest_w"), insertable = false, updatable = false)
    //@MapsId("weddingUuid")
    private Wedding wedding;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "guest_uuid", referencedColumnName = "uuid", foreignKey = @ForeignKey(name = "fk_wedding_guest_u"), insertable = false, updatable = false)
    //@MapsId("guestUuid")
    private User guest;

    private Integer tableNumber;

    private Integer totalInvitations;

    @Basic(optional = false)
    @Column(name = "status", columnDefinition = "enum('ATTENDING','NOT_ATTENDING','MAYBE','NOT_REPLIED')")
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

}