package com.example.gbuddy.dao;

import com.example.gbuddy.models.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
//@RibbonClient("gym-operation-service")
public interface GymDao extends JpaRepository<Gym, Integer> {

    public Gym selectGymBranchRecordById(@Param("gymId") int gymId, @Param("branchId") int branchId);
}
