package com.example.gbuddy.models.constants;

public enum ValidationInfoEnum {
    EMPTY_INVALID_USERNAME("EMPTY_INVALID_USERNAME"),
    EMPTY_INVALID_EMAIL("EMPTY_INVALID_EMAIL"),
    EMPTY_INVALID_MOBILE("EMPTY_INVALID_MOBILE"),
    EMPTY_INVALID_PASSWORD("EMPTY_INVALID_PASSWORD"),
    EMPTY_INVALID_ROLES("EMPTY_INVALID_ROLES"),
    EMPTY_INVALID_ABOUT("EMPTY_INVALID_ABOUT"),
    EMPTY_INVALID_IMAGE("EMPTY_INVALID_IMAGE"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS[username:%s]"),
    INVALID_LOGIN_CREDENTALS("INVALID_LOGIN_CREDENTALS"),
    USER_NOT_FOUND_IN_DB("USER_NOT_FOUND_IN_DB[username:%s]");

    private String validationInfoValue;

    ValidationInfoEnum(String validationInfoValue) {
        this.validationInfoValue = validationInfoValue;
    }

    public String getValidationInfoValue() {
        return validationInfoValue;
    }
}
