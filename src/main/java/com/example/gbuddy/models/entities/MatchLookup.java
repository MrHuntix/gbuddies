package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
public class MatchLookup {
    public static final String getRequestMatch = "FROM MatchLookup m where m.gymId = :gymId AND m.branchId = :branchId AND m.requesterId = :requesterId";
    public static final String possibleMatches = "FROM MatchLookup m where m.gymId = :gymId AND m.branchId = :branchId AND m.requesterId != :requesterId AND m.status = :status";
    public static final String deriveMatches = "FROM MatchLookup m WHERE m.gymId = :gymId AND m.branchId = :branchId AND m.status = :status";
    public static final String getMatchesByRequestIdAndStatus = "FROM MatchLookup m WHERE  m.requesterId = :requesterId AND m.status = :status";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "gymid")
    private int gymId;

    @Column(name = "branchid")
    private int branchId;

    @Column(name = "requesterid")
    private int requesterId;

    @Column(name = "status")
    private String status;
}
