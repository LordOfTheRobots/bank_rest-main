package com.example.bankcards.repository;

import com.example.bankcards.entity.CardCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardConditionRepository extends JpaRepository<CardCondition, UUID> {
    Optional<CardCondition> findById(UUID id);
    @Query("""
        SELECT cc FROM CardCondition cc 
        WHERE cc.card.cardId = :cardId 
        ORDER BY cc.dateOfCondition DESC 
        LIMIT 1
    """)
    Optional<CardCondition> findLatestByCardId(@Param("cardId") Long cardId);
}
