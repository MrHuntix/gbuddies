package com.example.gbuddy.models.constants;

public enum CommonConstants {
    FETCH_COMPLETED("fetch process completed. Found %s gyms"),
    NO_BRANCH_FOUND("no branch exiss having id %s"),
    FOUND_COORDINATES_FOR_BRANCH("found coordinates for branch id %s");

    private String message;

    CommonConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
