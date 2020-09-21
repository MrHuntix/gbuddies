package com.gbuddies.dao;

import com.gbuddies.models.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface GymDao extends JpaRepository<Gym, Integer> {
}
