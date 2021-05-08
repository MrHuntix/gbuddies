package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "MATCH_LOOKUP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "MatchLookup.getRequestMatch", query = MatchLookup.getRequestMatch),
        @NamedQuery(name = "MatchLookup.possibleMatches", query = MatchLookup.possibleMatches),
        @NamedQuery(name = "MatchLookup.deriveMatches", query = MatchLookup.deriveMatches),
        @NamedQuery(name = "MatchLookup.getMatchesByRequestIdAndStatus", query = MatchLookup.getMatchesByRequestIdAndStatus)
})
public class MatchLookup extends BaseEntity<Integer> {
    public static final String getRequestMatch = "FROM MatchLookup m where m.gymId = :gymId AND m.branchId = :branchId AND m.requesterId = :requesterId";
    public static final String possibleMatches = "FROM MatchLookup m where m.gymId = :gymId AND m.branchId = :branchId AND m.requesterId != :requesterId AND m.status = :status";
    public static final String deriveMatches = "FROM MatchLookup m WHERE m.gymId = :gymId AND m.branchId = :branchId AND m.status = :status";
    public static final String getMatchesByRequestIdAndStatus = "FROM MatchLookup m WHERE  m.requesterId = :requesterId AND m.status = :status";

    @Column(name = "gym_id")
    private int gymId;

    @Column(name = "branch_id")
    private int branchId;

    @Column(name = "requester_id")
    private int requesterId;

    @Column(name = "status")
    private String status;
}
