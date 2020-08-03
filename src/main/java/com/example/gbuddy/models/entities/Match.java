package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "MATCHES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Match.getMatched", query = Match.getMatched)
})
public class Match {
    public static final String getMatched = "FROM Match m WHERE m.requester = :id OR m.requestee = :id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "lookup_id")
    private int lookupId;

    @Column(name = "gymId")
    private int gymId;

    @Column(name = "branchid")
    private int branchId;

    @Column(name = "requester")
    private int requester;

    @Column(name = "requestee")
    private int requestee;
}
