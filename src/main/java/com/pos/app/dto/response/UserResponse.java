package com.pos.app.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String role;
    private boolean active;
    private String registeredBy;
    private LocalDateTime createdAt;
}