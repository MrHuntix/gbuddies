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
@NamedQueries({
        @NamedQuery(name = "MatchRequest.getMatchRequest", query = MatchRequest.getMatchRequest)
})
public class MatchRequest {
    public static final String getMatchRequest = "SELECT m FROM MatchRequest m WHERE m.lookupRequesterId = :lookupRequesterId AND m.lookupRequesteeId = :lookupRequesteeId AND m.userRequesterId = :userRequesterId AND m.userRequesteeId = :userRequesteeId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int lookupRequesterId;

    private int lookupRequesteeId;

    private int userRequesterId;

    private int userRequesteeId;

    private String status;
}
