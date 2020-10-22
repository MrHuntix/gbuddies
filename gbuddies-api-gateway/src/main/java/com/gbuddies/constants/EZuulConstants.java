package com.gbuddies.constants;

public enum EZuulConstants {
    PRE("pre");

    private String filterType;

    EZuulConstants(String filterType) {
        this.filterType = filterType;
    }

    public String getFilterType() {
        return filterType;
    }
}
