package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatResponse {
    int match_id;
    int lookup_id;
    String gymName;
    String website;
    Branch branch;
    User user;
}
