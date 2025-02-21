package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {
    @Schema(description = "User's email address", example = "paul@gmail.com")
    private String email;
    @Schema(description = "User's password", example = "123456")
    private String password;
} 