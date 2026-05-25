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
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        if(userRepository.findByUsername("superadmin").isEmpty()) {
            User superAdmin = User.builder()
                    .fullName("Super Admin")
                    .username("superadmin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.SUPER_ADMIN)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("superadmin has been created");
        }
    }
}
