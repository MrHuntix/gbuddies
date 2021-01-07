package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GEN_GYM")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Gym.selectGymBranchRecordById", query = Gym.selectGymBranchRecordById)
})
public class Gym {
    public static final String selectGymBranchRecordById = "SELECT g FROM Gym g JOIN g.branches b WHERE g.id = :gymId AND b.id = :branchId";
    public static final String selectGymByCity = "SELECT g FROM Gym g JOIN g.branches b WHERE g.id = :gymId AND b.id = :branchId";
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "website")
    private String website;

    @OneToMany(mappedBy = "gymId", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Branch> branches = new ArrayList<>();

}
