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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
public class UserAuthService {

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
        if (!passwordValidator.isValid(userAuthDto.getPassword())){
            throw new PasswordIsNotValid("Password is not valid by this requirements " +
                    passwordValidator.validateWithDetails(userAuthDto.getPassword()));
        }
        encodePassword(userAuthDto);
        if (!userRepository.existsByEmail(userAuthDto.getEmail())){
            User user = mapper.map(userAuthDto);
            userRepository.save(user);
            user = userRepository.findByEmail(user.getEmail()).get();
            return generateAuthResponse(userAuthDto.getEmail(), user.getUserId());
        }
        else {
            throw new UserAlreadyExist("User already exist");
        }
    }

    public void createUser(User user){
        if (!passwordValidator.isValid(user.getPassword())){
            throw new PasswordIsNotValid("Password is not valid by this requirements " +
                    passwordValidator.validateWithDetails(user.getPassword()));
        }
        encodePassword(user);
        if (!userRepository.existsByEmail(user.getEmail())){
            userRepository.save(user);
        }
        else {
            throw new UserAlreadyExist("User already exist");
        }
    }

    public AuthResponse authenticateUser(UserAuthDto userAuthDto){
        encodePassword(userAuthDto);
        Optional<User> user = userRepository.findByEmailAndPassword(userAuthDto.getEmail(), userAuthDto.getPassword());
        if (user.isPresent()){
            return generateAuthResponse(user.get().getEmail(), user.get().getUserId());
        }
        else {
            throw new UserDoesNotExistOrPasswordIncorrect("User Does Not Exist Or Password Incorrect");
        }
    }

    public AuthResponse refreshTokens(String refreshToken){
        if (jwtProvider.validateToken(refreshToken)){
            String username = jwtProvider.getUsernameFromToken(refreshToken);
            User user = userRepository.findByEmail(username).orElseThrow();
            return generateAuthResponse(username, user.getUserId());
        }
        else {
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
