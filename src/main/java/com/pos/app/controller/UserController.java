package com.pos.app.controller;

import com.pos.app.dto.response.*;
import com.pos.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.pos.app.model.entity.User;
import java.util.List;

@RestController @RequestMapping("/api/users") @RequiredArgsConstructor
public class UserController {
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> all() {
        return ResponseEntity.ok(ApiResponse.success("All users", authService.getAllUsers()));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> byId(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User", authService.getUserById(id)));
    }
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> byRole(@PathVariable String role) {
        return ResponseEntity.ok(ApiResponse.success("Users", authService.getUsersByRole(role)));
    }
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(@PathVariable Long id, @AuthenticationPrincipal User me) {
        return ResponseEntity.ok(ApiResponse.success("Deactivated", authService.deactivateUser(id, me)));
    }
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> activate(@PathVariable Long id, @AuthenticationPrincipal User me) {
        return ResponseEntity.ok(ApiResponse.success("Activated", authService.activateUser(id, me)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id, @AuthenticationPrincipal User me) {
        authService.deleteUser(id, me);
        return ResponseEntity.ok(ApiResponse.success("User deleted", null));
    }
}