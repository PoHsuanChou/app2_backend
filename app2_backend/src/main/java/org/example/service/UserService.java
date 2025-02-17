package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.RegisterUserReq;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterUserReq req) {
        // Check if user already exists
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            throw new UserAlreadyExistsException("User with email " + req.getEmail() + " already exists.");
        });

        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        // Create profile
        Profile profile = Profile.builder()
                .name(req.getNickname())
                .bio(req.getBio())
                .birthDate(req.getBirthday())
                .zodiacSign(req.getZodiacSign())
                .gender(req.getGender())
                .interests(req.getInterests())
                .profileImage(req.getProfileImage())
                .build();

        // Create user object
        User newUser = User.builder()
                .email(req.getEmail())
                .password(encodedPassword)
                .username(req.getNickname())
                .profile(profile)
                .isGoogleUser(req.isGoogleLogin())
                .createdAt(new Date())
                .lastActive(new Date())
                .isOnline(true)
                .build();

        return userRepository.save(newUser);
    }

    public User getUserData(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
