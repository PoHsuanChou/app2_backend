package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.RegisterUserReq;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.exception.UserAlreadyExistsException;
import org.example.exception.UserNotFoundException;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User registerUser(RegisterUserReq registerUserReq) {

        // Check if user already exists
        userRepository.findByEmail(registerUserReq.getEmail()).ifPresent(user -> {
            throw new UserAlreadyExistsException("User with email " + registerUserReq.getEmail() + " already exists.");
        });

        // Encrypt the password
        String encodedPassword = passwordEncoder.encode(registerUserReq.getPassword());

        // Create user object
        User newUser = User.builder()
                .email(registerUserReq.getEmail())
                .password(encodedPassword)
                .username(registerUserReq.getNickname())
                .profile(Profile.builder()
                        .name(registerUserReq.getNickname())
                        .birthDate(registerUserReq.getBirthday())
                        .gender(registerUserReq.getGender())
                        .build())
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
}
