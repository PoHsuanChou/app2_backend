package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.User;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleSSOResponse {
    private boolean success;
    private String message;
    private String token;
    private User user;
}
