package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface UserDao extends JpaRepository<User, Integer> {
    Optional<User> getByMobile(String mobile);

    Optional<User> getByUserId(int id);

    Optional<List<User>> getByUserIdIn(List<Integer> userIds);
}
