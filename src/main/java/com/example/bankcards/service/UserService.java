package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public boolean isOwnerOrAdmin(UUID userId, UUID currentId) {
        logger.debug("Checking if user {} is owner or admin for user {}", currentId, userId);
        return isOwner(userId, currentId) || isAdmin(currentId);
    }

    public User findUserById(UUID userId){
        logger.debug("Finding user by ID: {}", userId);
        return userRepository.findByUserId(userId).orElseThrow(
                () -> new NotFound("User not found"));
    }

    public boolean isOwner(UUID userId, UUID currentId) {
        logger.debug("Checking if user {} is owner of account {}", currentId, userId);
        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getUserId().equals(userId);
    }

    public boolean isAdmin(UUID currentId) {
        logger.debug("Checking if user {} is admin", currentId);
        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getRole().getRoleName().contains("ADMIN");
    }

    public void deleteUser(UUID userID){
        logger.info("Deleting user: {}", userID);
        userRepository.deleteByUserId(userID);
        logger.info("User deleted successfully: {}", userID);
    }

    public Boolean userExist(UUID userId){
        logger.debug("Checking if user exists: {}", userId);
        return userRepository.existsByUserId(userId);
    }

    public Page<User> showUsers(Integer pageNumber, Integer pageSize){
        logger.debug("Showing all cards, page: {}, size: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return userRepository.findAll(pageable);
    }

    public void patchUser(UserDto dto)  {
        User user = userRepository.findById(dto.getUserId()).orElseThrow(
                () -> new NotFound("No user with this id"));

        if (dto.getEmail() != null) {
            if (!user.getEmail().equals(dto.getEmail()) &&
                    userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getTelephoneNumber() != null) {
            if (!user.getTelephoneNumber().equals(dto.getTelephoneNumber()) &&
                    userRepository.existsByTelephoneNumber(dto.getTelephoneNumber())) {
                throw new DuplicateResourceException("Telephone already exists");
            }
            user.setTelephoneNumber(dto.getTelephoneNumber());
        }

        if (dto.getTelegramId() != null) {
            if (!user.getTelegramId().equals(dto.getTelegramId()) &&
                    userRepository.existsByTelegramId(dto.getTelegramId())) {
                throw new DuplicateResourceException("Tg already exists");
            }
            user.setTelegramId(dto.getTelegramId());
        }

        if (dto.getNotificationTypes() != null) {
        }
        userRepository.save(user);
    }
}
