package com.annabelle.backend.security;

import com.annabelle.backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public CurrentUserHolder currentUserHolder() {
        return new CurrentUserHolder();
    }

    @Bean
    public OncePerRequestFilter mockAuthFilter(UserRepository userRepository, CurrentUserHolder currentUserHolder) {
        return new MockAuthFilter(userRepository, currentUserHolder);
    }
}
