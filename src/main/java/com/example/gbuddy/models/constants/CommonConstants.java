package com.example.gbuddy.models.constants;

public enum CommonConstants {
    LOOKUP_REQUEST_EXISTS("request already exists(id: %s, status: %s)"),
    CANNOT_LIKE("unable to process request for like"),
    USER_KEY("user"),
    VALIDATION_KEY("message");

    private String message;

    CommonConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
