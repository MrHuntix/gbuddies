package com.example.gbuddy.models.constants;

public enum MatchLookupConstants {
    NO_MATCH_LOOKUP_RECORD("there are no records in match lookup table"),
    MATCH_LOOKUP_RESPONSE_CREATED("match lookup response created successfully"),
    NO_MATCHES_AVAILABLE("no matches available"),
    MATCH_RESPONSE_CREATED("match response created");

    private String message;

    MatchLookupConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
