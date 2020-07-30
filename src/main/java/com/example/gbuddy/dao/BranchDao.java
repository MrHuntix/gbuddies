package com.example.gbuddy.dao;

import com.example.gbuddy.models.Branch;
import com.example.gbuddy.models.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
//@RibbonClient("gym-operation-service")
public interface BranchDao extends JpaRepository<Branch, Integer> {
    Optional<Branch> selectGymBranchRecordById(@Param("branchId") int branchId, @Param("gymId") int gymId);
}
