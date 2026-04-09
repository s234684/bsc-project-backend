package com.annabelle.backend.repository;

import com.annabelle.backend.model.Role;
import com.annabelle.backend.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
