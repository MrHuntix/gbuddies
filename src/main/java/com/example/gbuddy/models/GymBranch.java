package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymBranch {
    private String locality;
    private String city;
    private float latitude;
    private float longitude;
    private String contact;
}
