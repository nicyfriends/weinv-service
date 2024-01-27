package com.mainthreadlab.weinv.commons;

public class Constants {

    private Constants() {
    }

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String ERROR_DESCRIPTION = "%d - %s";
    public static final String HTML_CALENDAR_TEMPLATE= "committee-calendar.html";
    public static final String APPLICATION_PDF_VALUE= "application/pdf";
    public static final String PDF_EXTENSION= "pdf";
    public static final String GUEST_FIELD = "guest";
    public static final String TABLE_NO = "Table No";
    public static final String NOM = "Nom";
    public static final String PRESENCE = "Présence";
    public static final String INVITATION_STATUS_FIELD = "invitationStatus";
    public static final String USERNAME_FIELD = "username";
    public static final String LASTNAME_FIELD = "lastName";
    public static final String FIRSTNAME_FIELD = "firstName";
    public static final String WEDDING_FIELD = "wedding";
    public static final String WEDDING_DATE_FIELD = "date";
    public static final String UUID_FIELD = "uuid";
    public static final String ENABLED_FIELD = "enabled";
    public static final String LIKE_KEYWORD_FORMAT = "%keyword%";
    public static final String TOTAL_INVITATIONS_TITLE = "Total invitations: %s\n";
    public static final String INVITATIONS_TAKEN_TITLE = "Invitations envoyées: %s\n";
    public static final String INVITATIONS_REMAINING_TITLE = "Invitations restantes: %s";
    public static final String W_INVITATION_PDF_MAIN_TITLE = "MARIAGE DE %s ET %s";
    public static final String INVITATION_PDF_MAIN_TITLE = "ANNIVERSAIRE DE %s %s";
    public static final String INVITATION_PDF_TITLE = TOTAL_INVITATIONS_TITLE + INVITATIONS_TAKEN_TITLE + INVITATIONS_REMAINING_TITLE;

}
