package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.NotificationType;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.NotificationTypeRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtProvider;
import com.example.bankcards.security.UserDetailsService;
import com.example.bankcards.util.PasswordValidator;
import com.example.bankcards.util.mapper.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAuthService {
    private static final Logger USER_LOG = LoggerFactory.getLogger("USER_LOG");

    private final UserRepository userRepository;
    private final DtoMapper<User, UserAuthDto> mapper;
    private final PasswordEncoder encoder;
    private final JwtProvider jwtProvider;
    private final PasswordValidator passwordValidator;
    private final UserDetailsService userDetailsService;
    private final RoleRepository roleRepository;
    private final @Qualifier("userDtoToUser") DtoMapper<User, UserDto> fromUserDtoToUser;
    private final NotificationTypeRepository notificationTypeRepository;


    public AuthResponse createUser(UserAuthDto userAuthDto){
        USER_LOG.info("Creating new user with email: {}", userAuthDto.getEmail());
        if (!passwordValidator.isValid(userAuthDto.getPassword())){
            USER_LOG.warn("Password validation failed for user: {}", userAuthDto.getEmail());
            throw new PasswordIsNotValid("Password is not valid by this requirements " +
                    passwordValidator.validateWithDetails(userAuthDto.getPassword()));
        }

        encodePassword(userAuthDto);
        if (!userRepository.existsByEmail(userAuthDto.getEmail())){
            User user = mapper.map(userAuthDto);
            user.setRole(roleRepository.findByRoleName("USER").get());
            user.setNotificationTypes(notificationTypeRepository.findAllByCodeIn(List.of(
                    "SERVICE_REMINDER"
            )));
            userRepository.save(user);
            user = userRepository.findByEmail(user.getEmail()).get();
            USER_LOG.info("User created successfully: {}", userAuthDto.getEmail());
            return generateAuthResponse(user.getUserId());
        } else {
            USER_LOG.warn("User already exists: {}", userAuthDto.getEmail());
            throw new UserAlreadyExist("User already exist");
        }
    }

    public void createUser(UserDto userDto){
        USER_LOG.info("Creating new user with email: {}", userDto.getEmail());
        if (!passwordValidator.isValid(userDto.getPassword())){
            USER_LOG.warn("Password validation failed for user: {}", userDto.getEmail());
            throw new PasswordIsNotValid("Password is not valid by this requirements " +
                    passwordValidator.validateWithDetails(userDto.getPassword()));
        }
        encodePassword(userDto);

        if (userDto.getEmail() != null) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
        } else {
            throw new NoIdentifier("There's no email");
        }

        if (userDto.getTelephoneNumber() != null) {
            if (userRepository.existsByTelephoneNumber(userDto.getTelephoneNumber())) {
                throw new DuplicateResourceException("Telephone already exists");
            }
        }

        if (userDto.getTelegramId() != null) {
            if (userRepository.existsByTelegramId(userDto.getTelegramId())) {
                throw new DuplicateResourceException("Tg already exists");
            }
        }

        if (userDto.getRoleName() != null){
            userDto.setRole(roleRepository.findByRoleName(userDto.getRoleName()).orElseThrow(
                    () -> new NotFound("There's no such role")
            ));
        }
        userRepository.save(fromUserDtoToUser.map(userDto));
    }

    public AuthResponse authenticateUser(UserAuthDto userAuthDto){
        USER_LOG.info("Authenticating user: {}", userAuthDto.getEmail());

        var userOpt = userRepository.findByEmail(userAuthDto.getEmail());
        if (userOpt.isPresent() && encoder.matches(userAuthDto.getPassword(), userOpt.get().getPassword())){
            USER_LOG.info("User authenticated successfully: {}", userAuthDto.getEmail());
            return generateAuthResponse(userOpt.get().getUserId());
        } else {
            USER_LOG.warn("Authentication failed for user: {}", userAuthDto.getEmail());
            throw new UserDoesNotExistOrPasswordIncorrect("User Does Not Exist Or Password Incorrect");
        }
    }

    public AuthResponse refreshTokens(String refreshToken){
        USER_LOG.debug("Refreshing tokens");
        if (jwtProvider.validateToken(refreshToken)){
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByEmail(username).orElseThrow();
            USER_LOG.info("Tokens refreshed successfully for user: {}", username);
            return generateAuthResponse(user.getUserId());
        } else {
            USER_LOG.warn("Refresh token expired or invalid");
            throw new RefreshTokenExpired("Refresh token is expired sign in again");
        }
    }

    private void encodePassword(UserAuthDto userAuthDto){ userAuthDto.setPassword(encoder.encode(userAuthDto.getPassword())); }
    private void encodePassword(UserDto userDto){ userDto.setPassword(encoder.encode(userDto.getPassword())); }
    private void encodePassword(User user){ user.setPassword(encoder.encode(user.getPassword())); }

    private AuthResponse generateAuthResponse(UUID userId){
        Authentication authentication = userDetailsService.createAuthentication(userId.toString());
        return new AuthResponse(
                jwtProvider.generateAccessToken(authentication),
                jwtProvider.generateRefreshToken(authentication)
        );
    }
}
