package com.pos.app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank
    private String fullName;

    @NotBlank
    private String username;

    @Email
    @NotBlank(message="Email is required")
    private String email;


    @NotBlank
    private String password;
}