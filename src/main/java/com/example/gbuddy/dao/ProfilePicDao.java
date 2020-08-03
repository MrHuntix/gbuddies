package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.ProfilePic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePicDao extends JpaRepository<ProfilePic, Integer> {
}
