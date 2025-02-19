package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.GoogleSSOResponse;
import org.example.dto.UserRegisterRequest;
import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user",
            description = "Register a new user with their profile information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = GoogleSSOResponse.class),
                            examples = @ExampleObject(value = """
                            {
                                "success": true,
                                "message": "User registered successfully",
                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                "user": {
                                    "id": "65d3524d8b91e2054f682a7c",
                                    "email": "test@example.com",
                                    "username": "Test User"
                                },
                                "isGoogle": true,
                                "email": "test@example.com"
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            required = true,
            content = @Content(
                    examples = @ExampleObject(value = """
                            {
                                "email": "test@example.com",
                                "password": null,
                                "nickname": "Test User",
                                "bio": "This is a test bio",
                                "gender": "Male",
                                "birthday": "1992-07-29T00:00:00.000Z",
                                "zodiacSign": "Leo",
                                "profileImage": "https://example.com/image.jpg",
                                "interests": ["塔羅牌", "占星術", "心理學"],
                                "isGoogleLogin": true
                            }
                            """)))
    @PostMapping("/register")
    public ResponseEntity<GoogleSSOResponse> registerUser(@RequestBody UserRegisterRequest request) {
        return userService.registerUser(request);
    }

}