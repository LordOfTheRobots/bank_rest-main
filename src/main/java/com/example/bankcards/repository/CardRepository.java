package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUserId(UUID userId, Pageable pageable);
    Page<Card> findAll(Pageable pageable);
    Optional<Card> findById(Long id);
    void deleteByCardNumberAndExpireDate(String cardNumber, String expireDate);
    Optional<Card> findByCardNumberAndExpireDate(String cardNumber, String expireDate);
    Boolean existsByCardNumber(String cardNumber);
    Optional<Card> findByCardNumberAndUserId(String cardNumber, UUID userId);
}
