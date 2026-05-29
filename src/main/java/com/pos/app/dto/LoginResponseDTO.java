package com.pos.app.dto;

import com.pos.app.model.enums.Role;
import lombok.Builder;
import lombok.Data;

public class LoginResponseDTO {
    @Data
    @Builder
    public static class UserDTO {
        private Long id;
        private String fullName;
        private String username;
        private Role role;

    }
}
