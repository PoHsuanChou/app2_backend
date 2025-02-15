package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;

    public User googleFindUser(String email) {
        return userRepository.findByEmail(email).orElseGet(null);
    }
}
