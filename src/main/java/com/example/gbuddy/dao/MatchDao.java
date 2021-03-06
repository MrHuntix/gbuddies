package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public interface MatchDao extends JpaRepository<Match, Integer> {
    public List<Match> getMatched(int id);

    List<Match> getAllByRequester(int requesterId);

    List<Match> getAllByRequestee(int requesteeId);
}
