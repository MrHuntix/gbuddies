package com.example.gbuddy.dao;

import com.example.gbuddy.models.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
//@RibbonClient("gym-operation-service")
public interface GymDao extends JpaRepository<Gym, Integer> {
}
