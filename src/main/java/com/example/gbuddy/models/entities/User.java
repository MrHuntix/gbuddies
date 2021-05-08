package com.example.gbuddy.models.entities;

import com.example.gbuddy.models.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity<Integer> {

    private String name;

    private String mobile;

    private String password;

    @Column(name = "pic_url")
    private String picUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String bio;

    @Temporal(TemporalType.DATE)
    @Column(name = "last_login_time")
    private Date lastLoginTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "deleted_time")
    private Date deletedTime;
}
