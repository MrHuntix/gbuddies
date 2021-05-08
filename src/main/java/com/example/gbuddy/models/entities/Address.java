package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "ADDRESS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address extends BaseId<Integer> {
    private String state;
    private String city;
    private long pincode;
}
