package com.example.bankcards.security;

import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@AllArgsConstructor
public class UserDetailService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            UserDetails userDetails = UserPrincipal.builder().
                    user(user.get()).
                    build();
            logger.debug("User loaded successfully: {}", email);
            return userDetails;
        } else {
            logger.warn("User not found: {}", email);
            throw new UsernameNotFoundException("User not presented in DB");
        }
    }

    public Authentication createAuthentication(String email) {
        logger.debug("Creating authentication for user: {}", email);
        UserDetails userDetails = loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }
}