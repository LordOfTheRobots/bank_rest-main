package com.example.bankcards;

import com.example.bankcards.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestRepositoryConfig {
    @Bean
    @Primary
    public UserRepository userRepository() {
        return mock(UserRepository.class);
    }
}
