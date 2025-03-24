package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.*;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.exception.UserAlreadyExistsException;
import org.example.repository.ProfileRepository;
import org.example.repository.UserRepository;
import org.example.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    private final UserService userService;

    private final AuthenticationService authenticationService;

    private final UserRepository userRepository;

    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService; // 用於處理文件上傳


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

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new LoginResponse(token,user.getId()));
    }

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
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GoogleSSOResponse> registerUser(
            @RequestPart("email") String email,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart("nickname") String nickname,
            @RequestPart(value = "bio", required = false) String bio,
            @RequestPart("birthday") String birthday,
            @RequestPart("gender") String gender,
            @RequestPart(value = "interests", required = false) String interests,
            @RequestPart("isGoogleLogin") String isGoogleLogin,
            @RequestPart(value = "zodiacSign", required = false) String zodiacSign,
            @RequestPart(value = "profileImage", required = false) MultipartFile file) {

        // Validate required fields
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname cannot be blank");
        }
        if (birthday == null || birthday.isBlank()) {
            throw new IllegalArgumentException("Birthday cannot be blank");
        }
        if (gender == null || gender.isBlank()) {
            throw new IllegalArgumentException("Gender cannot be blank");
        }

        // Check if user already exists
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new UserAlreadyExistsException("User with email " + email + " already exists.");
        });

        String encodedPassword = password != null ?
                passwordEncoder.encode(password) :
                passwordEncoder.encode("googleSSO");

        try {
            // Parse birthday
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsedBirthday = dateFormat.parse(birthday);

            // Handle profile image upload
            String profileImagePath = null;
            if (file != null && !file.isEmpty()) {
                String fileExtension = ".jpg";
                String newFileName = UUID.randomUUID().toString() + fileExtension;
                profileImagePath = fileStorageService.storeFile(file, newFileName);
            }

            // Parse interests
            List<String> interestList = null;
            if (interests != null && !interests.isBlank()) {
                interestList = Arrays.asList(new ObjectMapper().readValue(interests, String[].class));
            }

            // Create new user
            User newUser = User.builder()
                    .email(email)
                    .username(nickname)
                    .password(encodedPassword)
                    .profile(Profile.builder()
                            .name(nickname)
                            .bio(bio)
                            .birthDate(parsedBirthday)
                            .zodiacSign(zodiacSign)
                            .gender(gender)
                            .interests(interestList)
                            .profileImage(profileImagePath)
                            .build())
                    .isGoogleUser(Boolean.parseBoolean(isGoogleLogin))
                    .createdAt(new Date())
                    .lastActive(new Date())
                    .isOnline(false)
                    .picture(profileImagePath)
                    .roles(List.of("USER"))
                    .build();

            User savedUser = googleAuthService.createGoogleUser(newUser);
            String jwt = jwtService.generateToken(savedUser);

            String profileImageUrl = profileImagePath != null ?
                    ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("/uploads/")
                            .path(profileImagePath)
                            .toUriString() : null;

            return ResponseEntity.ok(GoogleSSOResponse.builder()
                    .success(true)
                    .message("User registered successfully")
                    .token(jwt)
                    .userId(savedUser.getId())
                    .isGoogle(Boolean.parseBoolean(isGoogleLogin))
                    .email(email)
                    .build());

        } catch (Exception e) {
            log.error("registerUser | Exception", e);
            return ResponseEntity.ok(GoogleSSOResponse.builder()
                    .success(false)
                    .message("User registration failed: " + e.getMessage())
                    .token(null)
                    .userId(null)
                    .isGoogle(Boolean.parseBoolean(isGoogleLogin))
                    .email(email)
                    .build());
        }
    }
}
