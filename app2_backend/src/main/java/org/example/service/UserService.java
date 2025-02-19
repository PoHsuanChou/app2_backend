package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.GoogleSSOResponse;
import org.example.dto.UserRegisterRequest;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.exception.ErrorResponse;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<GoogleSSOResponse> registerUser(UserRegisterRequest request) {
        // Check if user already exists
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists.");
        });
        String encodedPassword = null;
        // Encrypt the password
        if(request.getPassword() != null){
             encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        try {
            // Create profile
            Profile profile = Profile.builder()
                    .name(request.getNickname())
                    .bio(request.getBio())
                    .birthDate(request.getBirthday())
                    .zodiacSign(request.getZodiacSign())
                    .gender(request.getGender())
                    .interests(request.getInterests())
                    .profileImage(request.getProfileImage())
                    .build();

            // Create user
            User newUser = User.builder()
                    .email(request.getEmail())
                    .username(request.getNickname())
                    .password(encodedPassword)
                    .profile(profile)
                    .isGoogleUser(true)
                    .createdAt(new Date())
                    .lastActive(new Date())
                    .isOnline(true)
                    .build();

            User savedUser = googleAuthService.createGoogleUser(newUser);
            String jwt = jwtService.generateToken(savedUser);

            return ResponseEntity.ok(GoogleSSOResponse.builder()
                    .success(true)
                    .message("Google user registered successfully")
                    .token(jwt)
                    .user(savedUser)
                    .isGoogle(true)
                    .email(request.getEmail())
                    .build());

        } catch (Exception e) {
            log.error("registerUser | Exception",e);
            return ResponseEntity.ok(GoogleSSOResponse.builder()
                    .success(false)
                    .message("Google user registered failed")
                    .token(null)
                    .user(null)
                    .isGoogle(true)
                    .email(request.getEmail())
                    .build());
        }
    }


    public User getUserData(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
