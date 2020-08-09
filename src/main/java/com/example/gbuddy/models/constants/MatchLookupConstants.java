package com.example.gbuddy.models.constants;

public enum MatchLookupConstants {
    NO_MATCH_LOOKUP_RECORD("there are no records in match lookup table"),
    MATCH_LOOKUP_RESPONSE_CREATED("match lookup response created successfully"),
    NO_MATCHES_AVAILABLE("no matches available"),
    NO_FRIENDS_PRESNT("no friends present"),
    FRIEND_RESPONSE_CREATED("friend response created"),
    BUDDY_REQUEST_SENT("buddy request sent");

    private String message;

    MatchLookupConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
