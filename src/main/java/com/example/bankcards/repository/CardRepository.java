package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c WHERE c.user.userId = :userId")
    Page<Card> findByUserUserId(UUID userId, Pageable pageable);
    Page<Card> findAll(Pageable pageable);
    Optional<Card> findById(Long id);
    void deleteByCardNumberAndExpireDate(String cardNumber, String expireDate);
    Optional<Card> findByCardNumberAndExpireDate(String cardNumber, String expireDate);
    Boolean existsByCardNumber(String cardNumber);
    @Query("SELECT c FROM Card c WHERE c.cardNumber = :cardNumber AND c.user.userId = :userId")
    Optional<Card> findByCardNumberAndUserUserId(String cardNumber, UUID userId);
}
