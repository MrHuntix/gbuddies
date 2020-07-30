package com.example.gbuddy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "GYM_BRANCH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@NamedQueries({
        @NamedQuery(name = "Branch.selectGymBranchRecordById", query = Branch.selectGymBranchRecordById)
})
public class Branch {
    public static final String selectGymBranchRecordById = "FROM Branch b WHERE b.id = :branchId AND b.gymId.id = :gymId";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "locality")
    private String locality;

    @Column(name = "city")
    private String city;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "contact")
    private String contact;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, targetEntity = Gym.class)
    @JoinColumn(name = "gymId")
    @JsonIgnore
    private Gym gymId;

    @Override
    public String toString() {
        return "Branch{" +
                "id=" + id +
                ", locality='" + locality + '\'' +
                ", city='" + city + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", contact='" + contact + '\'' +
                '}';
    }
}
