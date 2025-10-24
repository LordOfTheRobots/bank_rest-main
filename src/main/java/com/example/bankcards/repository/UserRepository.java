package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);
    void deleteByUserId(UUID userID);
    Boolean existsByUserId(UUID userId);
    Boolean existsByEmail(String email);
}
