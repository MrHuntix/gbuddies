package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymResponse {
    private String responseMessage;
    private String responseStatus;
    private int responseCode;
    private List<Gym> gyms;

    public static List<Gym> getDefaultGyms() {
        return new ArrayList<>();
    }
}
