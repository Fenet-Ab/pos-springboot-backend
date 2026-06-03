package com.pos.app.repository;
import com.pos.app.model.entity.User;
import com.pos.app.model.enums.Role;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    boolean existsByUsername(@NotBlank String username);
    boolean existsByEmail(String email);
}