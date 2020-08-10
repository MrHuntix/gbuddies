package com.example.gbuddy.models.constants;

public enum CommonConstants {
    FETCH_COMPLETED("fetch process completed. Found %s gyms"),
    NO_BRANCH_FOUND("no branch exiss having id %s"),
    FOUND_COORDINATES_FOR_BRANCH("found coordinates for branch id %s"),
    LOOKUP_REQUEST_EXISTS("request already exists(id: %s, status: %s)"),
    REQUEST_RAISED("request for lookup raised"),
    NO_RECORD_IN_MATCH_LOOKUP("no record present in match lookup table"),
    CANNOT_LIKE("unable to process request for like"),
    IMPOSSIBLE_STATE("impossible state"),
    REQUEST_ALREADY_EXISTS("a request already exists, check your requests tab"),
    LIKED("like process completed"),
    UNMATCH_FAIL("unable to process request for unmatching"),
    REJECT_SUCCESS("rejected friend request");

    private String message;

    CommonConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
