package com.pos.app.controller;

import com.pos.app.dto.request.*;
import com.pos.app.dto.response.*;

import com.pos.app.model.entity.User;
import com.pos.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, registration, and password management")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Returns JWT token. Use it in Authorize as: Bearer {token}")
    @SecurityRequirements
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
    @Operation(summary = "Register user", description = "Requires SUPER_ADMIN, ADMIN, or MANAGER role")
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
    @Operation(summary = "Current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(@AuthenticationPrincipal User me) {
        return ResponseEntity.ok(ApiResponse.success("Current user", authService.getUserById(me.getId())));
    }

    @RequestMapping(value = "/change-password", method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Change password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal User me
    ){
        authService.changePassword(me,request);
        return ResponseEntity.ok(
                ApiResponse.success("Change password successful", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password", description = "Sends temporary password to user email")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ){
        authService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Temporary password sent to email", null));
    }
}
