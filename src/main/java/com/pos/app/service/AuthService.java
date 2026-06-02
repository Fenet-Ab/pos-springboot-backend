package com.pos.app.service;

import com.pos.app.dto.request.LoginRequestDTO;
import com.pos.app.dto.request.RegisterRequestDTO;
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
    public UserResponse registerAdmin(RegisterRequestDTO req, User by) {
        return registerUser(req, Role.ADMIN, by);
    }

    public UserResponse registerManager(RegisterRequestDTO req, User by) {
        return registerUser(req, Role.MANAGER, by);
    }

    public UserResponse registerCashier(RegisterRequestDTO req, User by) {
        return registerUser(req, Role.CASHIER, by);
    }

    private UserResponse registerUser(RegisterRequestDTO request, Role role, User createdBy) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .registeredBy(createdBy)
                .build();

        return toResponse(userRepository.save(user));
    }

    // ================= USERS =================
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public List<UserResponse> getUsersByRole(String role) {
        return userRepository.findByRole(Role.valueOf(role.toUpperCase()))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setActive(false);
        return toResponse(userRepository.save(user));
    }

    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setActive(true);
        return toResponse(userRepository.save(user));
    }

    // ================= MAPPER =================
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .role(user.getRole().name())
                .active(user.isActive())
                .registeredBy(user.getRegisteredBy() != null
                        ? user.getRegisteredBy().getFullName()
                        : "System")
                .createdAt(user.getCreatedAt())
                .build();
    }
}