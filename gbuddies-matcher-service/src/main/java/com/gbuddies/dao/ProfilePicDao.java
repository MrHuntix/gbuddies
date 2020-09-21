package com.gbuddies.dao;

import com.gbuddies.models.ProfilePic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfilePicDao extends JpaRepository<ProfilePic, Integer> {
}
