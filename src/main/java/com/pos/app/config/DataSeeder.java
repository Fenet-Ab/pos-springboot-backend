package com.pos.app.config;

import com.pos.app.model.entity.User;
import com.pos.app.model.enums.Role;
import com.pos.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        User existing = userRepository.findByUsername("superadmin").orElse(null);

        if (existing == null) {

            User superAdmin = User.builder()
                    .fullName("Super Admin")
                    .username("superadmin")
                    .email("superadmin@pos.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.SUPER_ADMIN)
                    .active(true)
                    .build();

            userRepository.save(superAdmin);
            System.out.println("superadmin created");

        } else {

            existing.setActive(true); // 🔥 FIX OLD BUG
            existing.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(existing);

            System.out.println("superadmin updated + activated");
        }
    }
}
