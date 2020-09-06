package com.example.gbuddy.models.constants;

import lombok.Getter;

@Getter
public enum ResponseMessageConstants {
    LOGIN_SUCCESSFULL("login successful", 1),
    INVALID_LOGIN_CREDENTIALS("login failed", 2),
    SIGNUP_SUCCESSFULL("signup successfull", 3),
    SIGNUP_UNSUCCESSFULL("signup failed", 4);
    private String message;
    private int id;

    ResponseMessageConstants(String message, int id) {
        this.message = message;
        this.id = id;
    }
}
