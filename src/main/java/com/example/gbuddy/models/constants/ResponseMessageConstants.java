package com.example.gbuddy.models.constants;

import lombok.Getter;

@Getter
public enum ResponseMessageConstants {
    LOGIN_SUCCESSFULL("login successful", 1),
    INVALID_LOGIN_CREDENTIALS("login failed", 2),
    SIGNUP_SUCCESSFULL("signup successfull", 3),
    SIGNUP_UNSUCCESSFULL("signup failed", 4),
    LOOKUP_CREATED("lookup has been created", 5),
    FAILED_TO_CREATE_LOOKUP("failed to create lookup", 6),
    BUDDY_REQUEST_SENT("buddy request sent", 7),
    REJECT_SUCCESS("rejected buddy request", 8),
    REJECT_FAILED("failed to reject buddy request", 9),
    FOUND_SUITABLE_BUDDIES("start liking to make some buddies", 10),
    FAILED_TO_FIND_SUITABLE_MATCHES("something happened while trying to find suitable matches", 11),
    DERIVED_BUDDIES("start liking to make some buddies", 12),
    FAILED_TO_DERIVE_MATCHES("something happened while trying to derive suitable matches", 11),
    DERIVE_FRIENDS("ohh! you have a lot of buddies, why dont you start talking to them and plan a workout session", 12),
    FAILED_TO_DERIVE_FRIENDS("something happened while retrieving your friends", 13),
    FAILED_TO_GET_FRIEND_REQUESTS("looks like we hit a wall while trying to get your friend requests", 14),
    BUDDY_REQUEST_ACCEPTED("buddy request accepted, you should be able to chat with each other soon", 15),
    GYM_CREATED("gym created and saved with ID[%s]", 16),
    FAILED_TO_CREATE_GYM("looks like there was an issue while adding your gym to our system", 17),
    FETCH_COMPLETED("found %s gyms", 18),
    FETCH_FAILED("something on the other side is preventing you from looking at all the gyms", 19),
    COORDINATES_FOUND("the gym exists in our reality", 20),
    COORDINATES_ERROR("looks like something happened while getting the coordinates of the gym", 21),
    USER_NOT_PRESENT("a user does not exist", 22),
    USER_LOOKUP_FAILED("something happened while trying to do user lookup", 23);

    private String message;
    private int id;

    ResponseMessageConstants(String message, int id) {
        this.message = message;
        this.id = id;
    }
}
