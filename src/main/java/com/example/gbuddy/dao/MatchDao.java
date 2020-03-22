package com.example.gbuddy.dao;

import com.example.gbuddy.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Transactional
public interface MatchDao extends JpaRepository<Match, Integer> {
}
