package com.example.gbuddy.dao;

import com.example.gbuddy.models.ProfilePic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePicDao extends JpaRepository<ProfilePic, Integer> {
}
