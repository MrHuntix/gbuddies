package com.gbuddies.models;

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
        @NamedQuery(name = "MatchRequest.getMatchRequest", query = MatchRequest.getMatchRequest),
        @NamedQuery(name = "MatchRequest.getByUserRequesteeIdAndStatus", query = MatchRequest.getByUserRequesteeIdAndStatus)
})
public class MatchRequest {
    public static final String getMatchRequest = "SELECT m FROM MatchRequest m WHERE m.lookupRequesterId = :lookupRequesterId AND m.lookupRequesteeId = :lookupRequesteeId AND m.userRequesterId = :userRequesterId AND m.userRequesteeId = :userRequesteeId";
    public static final String getByUserRequesteeIdAndStatus = "SELECT m FROM MatchRequest m WHERE m.userRequesteeId = :userRequesteeId AND status = :status";
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int lookupRequesterId;

    private int lookupRequesteeId;

    private int userRequesterId;

    private int userRequesteeId;

    private String status;
}
