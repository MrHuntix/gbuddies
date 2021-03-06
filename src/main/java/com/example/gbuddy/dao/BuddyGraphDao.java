package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.BuddyGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BuddyGraphDao extends JpaRepository<BuddyGraph, Integer> {
    List<BuddyGraph> getByUserId(int userId);
}
