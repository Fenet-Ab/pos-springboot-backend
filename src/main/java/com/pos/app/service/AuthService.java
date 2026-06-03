package com.pos.app.service;

import com.pos.app.dto.request.LoginRequestDTO;
import com.pos.app.dto.request.RegisterRequest;
import com.pos.app.dto.response.AuthResponse;
import com.pos.app.dto.response.UserResponse;
import com.pos.app.exception.ResourceNotFoundException;
import com.pos.app.exception.UsernameAlreadyExistsException;
import com.pos.app.model.entity.User;
import com.pos.app.model.enums.Role;
import com.pos.app.repository.UserRepository;
import com.pos.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    // ================= LOGIN =================

    public AuthResponse login(LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(user.getUsername());

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    // ================= REGISTER =================

    public UserResponse register(RegisterRequest request, User createdBy) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                    "Email already exists"
            );
        }

        validateRoleAction(createdBy, request.getRole());

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .registeredBy(createdBy)
                .build();

        return toResponse(userRepository.save(user));
    }

    // ================= ROLE VALIDATION =================

    private void validateRoleAction(
            User actor,
            Role targetRole
    ) {

        Role actorRole = actor.getRole();

        switch (actorRole) {

            case SUPER_ADMIN:
                return;

            case ADMIN:
                if (targetRole == Role.SUPER_ADMIN ||
                        targetRole == Role.ADMIN) {
                    throw new RuntimeException(
                            "Admin cannot perform this action on Admin or Super Admin"
                    );
                }
                return;

            case MANAGER:
                if (targetRole != Role.CASHIER) {
                    throw new RuntimeException(
                            "Manager can only perform this action on Cashiers"
                    );
                }
                return;

            default:
                throw new RuntimeException(
                        "You are not allowed to perform this action"
                );
        }
    }

    // ================= USERS =================

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + id
                        )
                );

        return toResponse(user);
    }

    public List<UserResponse> getUsersByRole(String role) {

        return userRepository.findByRole(
                        Role.valueOf(role.toUpperCase())
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse deactivateUser(Long id, User actor) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + id
                        )
                );
        validateRoleAction(actor, user.getRole());

        user.setActive(false);

        return toResponse(userRepository.save(user));
    }

    public UserResponse activateUser(Long id, User actor) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + id
                        )
                );
        validateRoleAction(actor, user.getRole());

        user.setActive(true);

        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id, User actor) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + id
                        )
                );
        validateRoleAction(actor, user.getRole());
        userRepository.delete(user);
    }

    // ================= MAPPER =================

    private UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .role(user.getRole().name())
                .active(user.isActive())
                .registeredBy(
                        user.getRegisteredBy() != null
                                ? user.getRegisteredBy().getFullName()
                                : "System"
                )
                .createdAt(user.getCreatedAt())
                .build();
    }
}