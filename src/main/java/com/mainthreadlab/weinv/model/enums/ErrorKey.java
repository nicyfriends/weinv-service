package com.mainthreadlab.weinv.model.enums;

import lombok.Getter;

@Getter
public enum ErrorKey {

    MAX_INVITATION_NUMBER_REACHED("Maximum number of invitations reached"),
    INVALID_WEDDING_DATE("Wedding date must not be before the current date"),
    USER_NOT_FOUND("User not found"),
    WEDDING_NOT_FOUND("Wedding not found"),
    INVITATION_NOT_FOUND("Invitation not found"),
    WEDDING_RESPONSIBLE_NOT_FOUND("Wedding responsible not found"),
    USER_ALREADY_RESPONSIBLE("User is already responsible for another wedding"),
    WRONG_USERNAME_OR_PWD("Wrong username or password"),
    USER_ALREADY_EXISTS("User already exists"),
    MISSING_ROLES_FIELD("Roles field is missing"),
    INCORRECT_TABLE_NUMBER("Incorrect table number"),
    WRONG_PASSWORD("Wrong password"),
    WRONG_USERNAME("Wrong username"),
    VALIDATION_ERROR("Missing required fields"),
    GENERIC_ERROR("Generic error");

    private final String message;

    ErrorKey(String message) {
        this.message = message;
    }

}
