package com.mainthreadlab.weinv.model.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class InvitationId implements Serializable {

    @Column(name = "event_uuid", nullable = false)
    private String eventUuid;

    @Column(name = "guest_uuid", nullable = false)
    private String guestUuid;

}
