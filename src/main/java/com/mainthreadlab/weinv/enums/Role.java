package com.mainthreadlab.weinv.enums;

import lombok.Getter;

@Getter
public enum Role {

    ADMIN("admin"),    // it can create 'user' and 'guest'
    USER("user"),      // it can create users of type 'guest'
    GUEST("guest");

    private final String name;

    Role(String name) {
        this.name = name;
    }

}
