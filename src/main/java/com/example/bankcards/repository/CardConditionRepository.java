package com.example.bankcards.repository;

import com.example.bankcards.entity.CardCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardConditionRepository extends JpaRepository<CardCondition, Integer> {
    Optional<CardCondition> findById(Integer id);
}
