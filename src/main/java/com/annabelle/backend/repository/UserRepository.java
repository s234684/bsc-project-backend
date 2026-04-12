package com.annabelle.backend.repository;
import com.annabelle.backend.model.User;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"roles", "tenant"})
    Optional<User> findByEmail(String email);
}

