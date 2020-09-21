package com.gbuddies.constants;

public enum MatchLookupConstants {
    NO_MATCH_LOOKUP_RECORD("there are no records in match lookup table"),
    NO_FRIENDS_PRESENT("no friends present"),
    NO_LOOKUP_RECORD("NO_LOOKUP_RECORD[ID:%s]"),
    UNACCEPTABLE_STATUS("UNACCEPTABLE_STATUS[ID:%s,STATUS:%s]");

    private String message;

    MatchLookupConstants(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
