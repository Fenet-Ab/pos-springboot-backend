package com.pos.app.dto.request;

import com.pos.app.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @Email
    @NotBlank(message="Email is required")
    private String email;

    @NotNull
    private Role role;

}
