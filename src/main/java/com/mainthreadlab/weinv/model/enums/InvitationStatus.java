package com.mainthreadlab.weinv.model.enums;

import lombok.Getter;

@Getter
public enum InvitationStatus {
    ATTENDING("participant"),
    NOT_ATTENDING("Ne participe pas"),
    MAYBE("Peut-être"),
    NOT_REPLIED("Pas de réponse");

    private final String description;

    InvitationStatus(String description) {
        this.description = description;
    }
}
