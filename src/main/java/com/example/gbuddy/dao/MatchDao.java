package com.example.gbuddy.dao;

import com.example.gbuddy.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public interface MatchDao extends JpaRepository<Match, Integer> {
    public List<Match> getMatched(int id);
}
