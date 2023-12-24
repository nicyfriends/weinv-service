package com.mainthreadlab.weinv.model;

import com.mainthreadlab.weinv.model.base.BaseEntity;
import com.mainthreadlab.weinv.model.base.WeddingGuestId;
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
@Table(name = "ws_wedding_guest")
public class WeddingGuest extends BaseEntity {

    @EmbeddedId
    private WeddingGuestId id;

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

    private boolean confirmed = false;

    private boolean rejected = false;

}