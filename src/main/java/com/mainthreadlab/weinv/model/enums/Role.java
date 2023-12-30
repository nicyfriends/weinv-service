package com.mainthreadlab.weinv.model.enums;

import lombok.Getter;

@Getter
public enum Role {

    ADMIN("admin"),    // it can create 'user' and 'guest'
    USER("user"),      // it can create users of type 'guest'
    GUEST("guest");

    private final String description;

    Role(String description) {
        this.description = description;
    }

}
