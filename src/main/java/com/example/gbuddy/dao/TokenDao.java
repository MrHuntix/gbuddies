package com.example.gbuddy.dao;

import com.example.gbuddy.models.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface TokenDao extends JpaRepository<Token, Integer> {
}
