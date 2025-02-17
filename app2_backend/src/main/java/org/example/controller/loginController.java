package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RegisterUserReq;
import org.example.dto.EmailCheckRequest;
import org.example.dto.EmailCheckResponse;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<User> getAllUsers(@Valid @RequestBody RegisterUserReq registerUserReq ) {
        log.info("Entering registerUser with request: {}", registerUserReq);
        User createdUser = userService.registerUser(registerUserReq);
        return ResponseEntity.ok(createdUser);
//        return null;
    }

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
