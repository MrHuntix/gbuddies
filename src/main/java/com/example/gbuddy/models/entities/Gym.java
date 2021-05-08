package com.example.gbuddy.models.entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "GYM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Gym.selectGymBranchRecordById", query = Gym.selectGymBranchRecordById)
})
public class Gym extends BaseEntity<Integer> {
    public static final String selectGymBranchRecordById = "SELECT g FROM Gym g JOIN g.branches b WHERE g.id = :gymId AND b.id = :branchId";
    public static final String selectGymByCity = "SELECT g FROM Gym g JOIN g.branches b WHERE g.id = :gymId AND b.id = :branchId";

    @Column(name = "name")
    private String name;

    @Column(name = "website")
    private String website;

    @OneToMany(mappedBy = "gymId", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Branch> branches = new ArrayList<>();

}
