package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.NotificationTypeRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger APP_LOG = LoggerFactory.getLogger("APP_LOG");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final @Qualifier("userToUserDto") DtoMapper<UserDto, User> userUserDtoDtoMapper;

    public boolean isOwnerOrAdmin(UUID userId, UUID currentId) {
        APP_LOG.debug("Checking if user {} is owner or admin for account {}", currentId, userId);
        return isOwner(userId, currentId) || isAdmin(currentId);
    }

    public User findUserById(UUID userId) {
        APP_LOG.debug("Finding user by ID: {}", userId);
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFound("User not found"));
    }

    public boolean isOwner(UUID userId, UUID currentId) {
        APP_LOG.debug("Checking ownership: currentId={}, targetId={}", currentId, userId);
        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getUserId().equals(userId);
    }

    public boolean isAdmin(UUID currentId) {
        APP_LOG.debug("Checking admin role for user: {}", currentId);
        User user = userRepository.findById(currentId)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getRole() != null && user.getRole().getRoleName().contains("ADMIN");
    }

    @Transactional
    public void deleteUser(UUID userId) {
        APP_LOG.info("Deleting user: {}", userId);
        userRepository.deleteByUserId(userId);
        APP_LOG.info("User deleted successfully: {}", userId);
    }

    public boolean userExist(UUID userId) {
        APP_LOG.debug("Checking user existence: {}", userId);
        return userRepository.existsByUserId(userId);
    }

    public Page<User> showUsers(Integer pageNumber, Integer pageSize) {
        APP_LOG.debug("Fetching users page: {}, size: {}", pageNumber, pageSize);
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return userRepository.findAll(pageable);
    }

    @Transactional
    public void patchUser(UserDto dto) {
        APP_LOG.info("Updating user: {}", dto.getUserId());
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFound("No user with this id"));

        if (dto.getEmail() != null) {
            if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(dto.getEmail());
        }

        if (dto.getTelephoneNumber() != null) {
            if (userRepository.existsByTelephoneNumber(dto.getTelephoneNumber())) {
                throw new DuplicateResourceException("Telephone already exists");
            }
            user.setTelephoneNumber(dto.getTelephoneNumber());
        }

        if (dto.getTelegramId() != null) {
            if (!user.getTelegramId().equals(dto.getTelegramId()) && userRepository.existsByTelegramId(dto.getTelegramId())) {
                throw new DuplicateResourceException("Telegram ID already exists");
            }
            user.setTelegramId(dto.getTelegramId());
        }

        if (dto.getRoleName() != null) {
            user.setRole(roleRepository.findByRoleName(dto.getRoleName())
                    .orElseThrow(() -> new NotFound("There's no such role")));
        }

        if (dto.getNotificationNames() != null && !dto.getNotificationNames().isEmpty()) {
            user.setNotificationTypes(notificationTypeRepository.findAllByCodeIn(dto.getNotificationNames()));
        }

        userRepository.save(user);
        APP_LOG.info("User updated successfully: {}", dto.getUserId());
    }

    public UserDto getUser(UUID userId){
        return userUserDtoDtoMapper.map(userRepository.findByUserId(userId).orElseThrow(() ->
                new NotFound("No such user")));
    }
}