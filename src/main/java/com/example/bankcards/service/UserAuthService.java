package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.PasswordIsNotValid;
import com.example.bankcards.exception.RefreshTokenExpired;
import com.example.bankcards.exception.UserAlreadyExist;
import com.example.bankcards.exception.UserDoesNotExistOrPasswordIncorrect;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailService;
import com.example.bankcards.util.PasswordValidator;
import com.example.bankcards.util.mapper.DtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserAuthService {
    private static final Logger logger = LoggerFactory.getLogger(UserAuthService.class);

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final DtoMapper<User, UserAuthDto> mapper;

    @Autowired
    private final PasswordEncoder encoder;

    @Autowired
    private final JwtTokenProvider jwtProvider;

    @Autowired
    private final PasswordValidator passwordValidator;

    @Autowired
    private final UserDetailService userDetailService;

    public UserAuthService(UserRepository userRepository,
                           @Qualifier("userAuthMap") DtoMapper<User, UserAuthDto> mapper,
                           PasswordEncoder encoder,
                           JwtTokenProvider jwtProvider,
                           PasswordValidator passwordValidator,
                           UserDetailService userDetailService) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.encoder = encoder;
        this.jwtProvider = jwtProvider;
        this.passwordValidator = passwordValidator;
        this.userDetailService = userDetailService;
    }

    public AuthResponse createUser(UserAuthDto userAuthDto){
        logger.info("Creating new user with email: {}", userAuthDto.getEmail());
        if (!passwordValidator.isValid(userAuthDto.getPassword())){
            logger.warn("Password validation failed for user: {}", userAuthDto.getEmail());
            throw new PasswordIsNotValid("Password is not valid by this requirements " +
                    passwordValidator.validateWithDetails(userAuthDto.getPassword()));
        }

        encodePassword(userAuthDto);
        if (!userRepository.existsByEmail(userAuthDto.getEmail())){
            User user = mapper.map(userAuthDto);
            userRepository.save(user);
            user = userRepository.findByEmail(user.getEmail()).get();
            logger.info("User created successfully: {}", userAuthDto.getEmail());
            return generateAuthResponse(userAuthDto.getEmail(), user.getUserId());
        }
        else {
            logger.warn("User already exists: {}", userAuthDto.getEmail());
            throw new UserAlreadyExist("User already exist");
        }
    }

    public void createUser(User user){
        logger.info("Creating new user with email: {}", user.getEmail());

        if (!passwordValidator.isValid(user.getPassword())){
            logger.warn("Password validation failed for user: {}", user.getEmail());
            throw new PasswordIsNotValid("Password is not valid by this requirements " +
                    passwordValidator.validateWithDetails(user.getPassword()));
        }

        encodePassword(user);
        if (!userRepository.existsByEmail(user.getEmail())){
            userRepository.save(user);
            logger.info("User created successfully: {}", user.getEmail());
        }
        else {
            logger.warn("User already exists: {}", user.getEmail());
            throw new UserAlreadyExist("User already exist");
        }
    }

    public AuthResponse authenticateUser(UserAuthDto userAuthDto){
        logger.info("Authenticating user: {}", userAuthDto.getEmail());

        encodePassword(userAuthDto);
        Optional<User> user = userRepository.findByEmailAndPassword(userAuthDto.getEmail(), userAuthDto.getPassword());
        if (user.isPresent()){
            logger.info("User authenticated successfully: {}", userAuthDto.getEmail());
            return generateAuthResponse(user.get().getEmail(), user.get().getUserId());
        }
        else {
            logger.warn("Authentication failed for user: {}", userAuthDto.getEmail());
            throw new UserDoesNotExistOrPasswordIncorrect("User Does Not Exist Or Password Incorrect");
        }
    }

    public AuthResponse refreshTokens(String refreshToken){
        logger.debug("Refreshing tokens");

        if (jwtProvider.validateToken(refreshToken)){
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByEmail(username).orElseThrow();
            logger.info("Tokens refreshed successfully for user: {}", username);
            return generateAuthResponse(username, user.getUserId());
        }
        else {
            logger.warn("Refresh token expired or invalid");
            throw new RefreshTokenExpired("Refresh token is expired sign in again");
        }
    }

    private void encodePassword(UserAuthDto userAuthDto){
        userAuthDto.setPassword(encoder.encode(userAuthDto.getPassword()));
    }

    private void encodePassword(User user){
        user.setPassword(encoder.encode(user.getPassword()));
    }

    private AuthResponse generateAuthResponse(String username, UUID userId){
        Authentication authentication = userDetailService.createAuthentication(username);
        return new AuthResponse(
                jwtProvider.generateAccessToken(authentication),
                jwtProvider.generateRefreshToken(authentication),
                userId
        );
    }
}
