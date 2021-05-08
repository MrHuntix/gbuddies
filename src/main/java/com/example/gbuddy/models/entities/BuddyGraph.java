package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "BUDDY_GRAPH")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuddyGraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "buddy_id")
    private int userBuddy;

    @Column(name = "match_request_id")
    private int matchRequestId;
}
