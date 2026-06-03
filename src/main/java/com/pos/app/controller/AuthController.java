package com.pos.app.controller;

import com.pos.app.dto.request.*;
import com.pos.app.dto.response.*;

import com.pos.app.model.entity.User;
import com.pos.app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequestDTO req) {

        System.out.println("LOGIN REQUEST RECEIVED");

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        authService.login(req)
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest req,
            @AuthenticationPrincipal User me) {

        return ResponseEntity.status(201)
                .body(ApiResponse.success(
                        "User registered",
                        authService.register(req, me)
                ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal User me) {
        return ResponseEntity.ok(ApiResponse.success("Current user", authService.getUserById(me.getId())));
    }
}