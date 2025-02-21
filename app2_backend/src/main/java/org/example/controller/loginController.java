package org.example.controller;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.EmailCheckRequest;
import org.example.dto.EmailCheckResponse;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.service.AuthenticationService;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    private final UserService userService;

    private final AuthenticationService authenticationService;


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

    @PostMapping("/login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged in, returns JWT token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid email or password"),
            @ApiResponse(responseCode = "400", description = "Bad Request, invalid input")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authenticationService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
