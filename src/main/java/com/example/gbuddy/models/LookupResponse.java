package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LookupResponse {
    private String message;
    private int responseCode;
    private String responseStatus;
    private List<MatchLookup> gyms;
}
