package com.gbuddies.constants;

public enum CommonConstants {
    LOOKUP_REQUEST_EXISTS("request already exists(id: %s, status: %s)"),
    CANNOT_LIKE("unable to process request for like");

    private String message;

    CommonConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
