package com.example.bankcards.security;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private static final Logger USER_LOG = LoggerFactory.getLogger("USER_LOG");
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String uuid) throws UsernameNotFoundException {
        USER_LOG.debug("Loading user by uuid: {}", uuid);
        Optional<User> user = userRepository.findById(UUID.fromString(uuid));

        if (user.isPresent()) {
            UserDetails userDetails = UserPrincipal.builder().user(user.get()).build();
            USER_LOG.debug("User loaded successfully: {}", uuid);
            return userDetails;
        } else {
            USER_LOG.warn("User not found: {}", uuid);
            throw new UsernameNotFoundException("User not presented in DB");
        }
    }

    public Authentication createAuthentication(String uuid) {
        USER_LOG.debug("Creating authentication for user: {}", uuid);
        UserDetails userDetails = loadUserByUsername(uuid);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}