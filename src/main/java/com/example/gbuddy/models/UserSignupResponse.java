package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupResponse {
    private String responseMessage;
    private String responseStatus;
    private int responseCode;
}
