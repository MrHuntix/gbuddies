package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.MatchRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface MatchRequestDao extends JpaRepository<MatchRequest, Integer> {
    Optional<MatchRequest> getMatchRequest(@Param("lookupRequesterId")int lookupRequesterId, @Param("lookupRequesteeId")int lookupRequesteeId, @Param("userRequesterId")int userRequesterId, @Param("userRequesteeId")int userRequesteeId);
}
