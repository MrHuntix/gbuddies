package com.example.gbuddy.models.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseId<T> implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;
}
