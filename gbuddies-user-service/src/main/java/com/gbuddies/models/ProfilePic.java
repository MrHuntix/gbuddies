package com.gbuddies.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
