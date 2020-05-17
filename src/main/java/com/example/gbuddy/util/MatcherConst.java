package com.example.gbuddy.util;

import lombok.Getter;

@Getter
public enum MatcherConst {
    MATCHED("MATCHED", 1),
    UNMATCHED("UNMATCHED", 2),
    REQUESTED("REQUESTED", 3);

    private String name;
    private int id;

    MatcherConst(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
