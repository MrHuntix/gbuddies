package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GymDao extends JpaRepository<Gym, Integer> {
}
