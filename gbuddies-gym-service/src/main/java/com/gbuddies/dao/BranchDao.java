package com.gbuddies.dao;

import com.gbuddies.models.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface BranchDao extends JpaRepository<Branch, Integer> {
    Optional<Branch> selectGymBranchRecordById(@Param("branchId") int branchId, @Param("gymId") int gymId);
}
