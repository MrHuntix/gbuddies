package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.MatchLookup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@Transactional
public interface MatchLookupDao extends JpaRepository<MatchLookup, Integer> {
    List<MatchLookup> getByStatus(String status);

    List<MatchLookup> getAllByRequesterId(int requesterId);

    List<MatchLookup> possibleMatches(@Param("gymId") int gymId, @Param("branchId") int branchId, @Param("requesterId") int requesterId, @Param("status") String status);

    List<MatchLookup> deriveMatches(@Param("gymId") int gymId, @Param("branchId") int branchId, @Param("status") String status);

    List<MatchLookup> getMatchesByRequestIdAndStatus(@Param("requesterId") int requesterId, @Param("status") String status);

    MatchLookup getById(int id);

    MatchLookup getRequestMatch(@Param("gymId") int gymId, @Param("branchId") int branchId, @Param("requesterId") int requesterId);
}
