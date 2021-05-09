package com.example.gbuddy.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Blob;

@Entity
@Table(name = "GEN_PROFILE_PIC")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfilePic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int picId;
    @Column(length=16777215)
    private Blob userImage;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "profilePic")
    @JsonIgnoreProperties("user")
    private User user;

    @Override
    public String toString() {
        return "ProfilePic{" +
                "picId=" + picId +
                ", userImage=" + userImage +
                '}';
    }
}
