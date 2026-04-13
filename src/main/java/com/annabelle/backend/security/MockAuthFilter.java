package com.annabelle.backend.security;

import com.annabelle.backend.model.RoleName;
import com.annabelle.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class MockAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final CurrentUserHolder currentUserHolder;

    public MockAuthFilter(UserRepository userRepository, CurrentUserHolder currentUserHolder) {
        this.userRepository = userRepository;
        this.currentUserHolder = currentUserHolder;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String email = request.getHeader("X-Debug-User");

            if (email != null && !email.isBlank()) {
                userRepository.findByEmail(email).ifPresent(user -> {
                    Set<RoleName> roleNames = user.getRoles()
                            .stream()
                            .map(role -> role.getName())
                            .collect(Collectors.toSet());

                    CurrentUser currentUser = new CurrentUser(
                            user.getId(),
                            user.getTenant() != null ? user.getTenant().getId() : null,
                            user.getTenant() != null ? user.getTenant().getName() : null,
                            user.getEmail(),
                            roleNames
                    );

                    currentUserHolder.setCurrentUser(currentUser);
                });
            }

            filterChain.doFilter(request, response);
        } finally {
            currentUserHolder.clear();
        }
    }
}

