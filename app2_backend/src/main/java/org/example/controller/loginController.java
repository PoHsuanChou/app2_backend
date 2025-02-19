package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.EmailCheckRequest;
import org.example.dto.EmailCheckResponse;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    private final UserService userService;


    @PostMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(@RequestBody EmailCheckRequest request) {
        log.info("Checking email existence: {}", request.getEmail());
        boolean exists = userService.existsByEmail(request.getEmail());
        
        EmailCheckResponse response = EmailCheckResponse.builder()
                .exists(exists)
                .message(exists ? "Email already registered" : "Email available")
                .email(request.getEmail())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
