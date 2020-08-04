package com.example.gbuddy.models.constants;

public enum MatchRequestConstants {
    REQUESTED("REQUESTED"),
    REQUEST_ALREADY_EXISTS("REQUEST_ALREADY_EXISTS[ID:%s]"),
    ACCEPTED("ACCEPTED");

    private String status;

    MatchRequestConstants(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
