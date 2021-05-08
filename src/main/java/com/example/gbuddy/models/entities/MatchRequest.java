package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "MATCH_REQUEST")
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@NamedQueries({
        @NamedQuery(name = "MatchRequest.getMatchRequest", query = MatchRequest.getMatchRequest),
        @NamedQuery(name = "MatchRequest.getByUserRequesteeIdAndStatus", query = MatchRequest.getByUserRequesteeIdAndStatus)
})
public class MatchRequest extends BaseEntity<Integer> {
    public static final String getMatchRequest = "SELECT m FROM MatchRequest m WHERE m.lookupRequesterId = :lookupRequesterId AND m.lookupRequesteeId = :lookupRequesteeId AND m.userRequesterId = :userRequesterId AND m.userRequesteeId = :userRequesteeId";
    public static final String getByUserRequesteeIdAndStatus = "SELECT m FROM MatchRequest m WHERE m.userRequesteeId = :userRequesteeId AND status = :status";

    @Column(name = "lookup_requester_id")
    private int lookupRequesterId;

    @Column(name = "lookup_requestee_id")
    private int lookupRequesteeId;

    @Column(name = "user_requester_id")
    private int userRequesterId;

    @Column(name = "user_requestee_id")
    private int userRequesteeId;

    private String status;
}
