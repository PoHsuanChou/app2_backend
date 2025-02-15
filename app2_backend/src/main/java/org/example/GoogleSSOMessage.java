package org.example;

public enum GoogleSSOMessage {
    NEW_USER("01"),
    AUTHENTICATED("02"),
    AUTHENTICATION_FAILED("03"),
    INVALID_ID_TOKEN("04");

    private final String message;

    GoogleSSOMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
