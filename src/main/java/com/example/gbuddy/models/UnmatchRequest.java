package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnmatchRequest {
    int matchId;
    int gymId;
    private int  requesterId;
    private String reason;
}
