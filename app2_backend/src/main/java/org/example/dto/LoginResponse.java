package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.entity.User;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String userId;
} 