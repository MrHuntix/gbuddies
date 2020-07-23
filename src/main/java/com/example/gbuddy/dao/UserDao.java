package com.example.gbuddy.dao;

import com.example.gbuddy.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public interface UserDao extends JpaRepository<User, Integer> {
    Optional<User> getByUserNameAndPassword(String userName, String password);
    Optional<User> getByUserName(String userName);
    Optional<User> getByUserId(int id);
    Optional<List<User>> getByUserIdIn(List<Integer> userIds);
}
