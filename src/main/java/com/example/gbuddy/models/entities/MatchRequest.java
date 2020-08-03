package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "MATCH_REQUEST")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int lookupRequesterId;

    private int lookupRequesteeId;

    private int userRequesterId;

    private int userRequesteeId;

    private String status;
}
