package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface MatchRequestDao extends JpaRepository<MatchRequest, Integer> {
}
