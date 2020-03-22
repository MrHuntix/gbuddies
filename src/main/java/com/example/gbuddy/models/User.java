package com.example.gbuddy.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "GEN_USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private int userId;
    private String userName;
    private String emailId;
    private String mobileNo;
    private String password;
    private String roles;
}
