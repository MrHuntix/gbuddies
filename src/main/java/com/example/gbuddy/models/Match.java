package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity(name = "MATCHES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
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
