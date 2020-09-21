package com.gbuddies.constants;

public enum ValidationInfoEnum {
    EMPTY_USERNAME("EMPTY_USERNAME"),
    EMPTY_EMAIL("EMPTY_EMAIL"),
    EMPTY_MOBILE("EMPTY_MOBILE"),
    EMPTY_PASSWORD("EMPT_PASSWORD"),
    EMPTY_ROLES("EMPTY_ROLES"),
    EMPTY_ABOUT("EMPTY_ABOUT"),
    EMPTY_IMAGE("EMPTY_IMAGE"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS[username:%s]"),
    INVALID_LOGIN_CREDENTALS("INVALID_LOGIN_CREDENTALS"),
    USER_NOT_FOUND_IN_DB("USER_NOT_FOUND_IN_DB[username:%s]"),
    EMPTY_GYM_NAME("EMPTY_GYM_NAME"),
    EMPTY_WEBSITE("EMPTY_WEBSITE"),
    EMPTY_LOCALITY("EMPTY_LOCALITY[locality:%s]"),
    EMPTY_CITY("EMPTY_CITY[city:%s]"),
    EMPTY_LATITUDE("EMPTY_LATITUDE[latitude:%s]"),
    EMPTY_LONGIUDE("EMPTY_LONGIUDE[longitude:%s]"),
    EMPTY_CONTACT("EMPTY_CONTACT[contact:%s]"),
    EMPTY_BRANCHES("EMPTY_BRANCHES");

    private String validationInfoValue;

    ValidationInfoEnum(String validationInfoValue) {
        this.validationInfoValue = validationInfoValue;
    }

    public String getValidationInfoValue() {
        return validationInfoValue;
    }
}
