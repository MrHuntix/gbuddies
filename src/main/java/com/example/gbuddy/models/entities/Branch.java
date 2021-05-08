package com.example.gbuddy.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "BRANCH")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedQueries({
        @NamedQuery(name = "Branch.selectGymBranchRecordById", query = Branch.selectGymBranchRecordById)
})
public class Branch extends BaseId<Integer>{
    public static final String selectGymBranchRecordById = "FROM Branch b WHERE b.id = :branchId AND b.gymId.id = :gymId";

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "contact")
    private String contact;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Gym.class)
    @JoinColumn(name = "gym_id")
    @JsonIgnore
    private Gym gymId;
}
