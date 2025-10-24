package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    @Autowired
    private CorsConfigurationSource corsConfig;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.
                csrf(CsrfConfigurer::disable).
                sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)).
                cors(cors ->
                        cors.
                                configurationSource(corsConfig)).
                authorizeHttpRequests(auth ->
                        auth.
                                requestMatchers("/api/**/admin/**").hasRole("ADMIN").
                                requestMatchers("/api/**/user/**").authenticated().
                                requestMatchers("/error").permitAll().
                                requestMatchers("/v3/api-docs/**").authenticated()).
                formLogin(form -> form.loginPage("/api/sign-up/").
                        failureForwardUrl("/error").permitAll()).
                addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
