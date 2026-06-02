package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Deprecated
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(UUID userId);
    void deleteByUserId(UUID userID);
    Boolean existsByUserId(UUID userId);
    Boolean existsByEmail(String email);
    Boolean existsByTelephoneNumber(String telephone);
    Boolean existsByTelegramId(String email);
    @Query("SELECT u FROM User u")
    @QueryHints({@QueryHint(name = "org.hibernate.fetchSize", value = "1000")})
    Stream<User> streamAllUsers();
}
