package com.example.bankcards.config;

import com.example.bankcards.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CorsConfigurationSource corsConfigurationSource;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.
                headers(
                        headers -> headers.contentSecurityPolicy
                        (csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "script-src 'self'; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "img-src 'self' https://res.cloudinary.com/ data: blob:;"
        ))).
                csrf(CsrfConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new RegexRequestMatcher("^/$", null),
                                new RegexRequestMatcher("^/index\\.html$", null),
                                new RegexRequestMatcher("^/static/.*$", null),
                                new RegexRequestMatcher("^/css/.*$", null),
                                new RegexRequestMatcher("^/js/.*$", null),
                                new RegexRequestMatcher("^/images/.*$", null),
                                new RegexRequestMatcher("/favicon.ico", null)
                        ).permitAll()

                        .requestMatchers("/profile/**", "/profile")
                        .authenticated()

                        .requestMatchers(
                                new RegexRequestMatcher("^.*/sign-in$", null),
                                new RegexRequestMatcher("^.*/sign-up$", null),
                                new RegexRequestMatcher("^.*/refresh$", null),
                                new RegexRequestMatcher("^.*/logout$", null)
                        ).permitAll()

                        .requestMatchers(
                                new RegexRequestMatcher("^/error$", null),
                                new RegexRequestMatcher("^/v3/api-docs/.*$", null),
                                new RegexRequestMatcher("^/swagger-ui/.*$", null),
                                new RegexRequestMatcher("^/swagger-ui\\.html$", null)
                        ).permitAll()

                        .requestMatchers(
                                new RegexRequestMatcher("^.*/admin/.*$", null)
                        ).hasAuthority("ADMIN")

                        .requestMatchers(
                                new RegexRequestMatcher("^.*/user/.*$", null),
                                new RegexRequestMatcher("^.*/transaction/.*$", null)
                        ).authenticated()
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/cards")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
