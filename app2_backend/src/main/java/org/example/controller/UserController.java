package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    @GetMapping
    public List<User> getAllUsers() {
        System.out.println("GGGG");
        System.out.println(userRepository.findAll());
        return userRepository.findAll();
    }
//    @GetMapping("/user")
//    public ResponseEntity<User> getUserData(@AuthenticationPrincipal UserPrincipal userPrincipal) {
//        User user = userService.getUserData(userPrincipal.getUsername());
//        return ResponseEntity.ok(user);
//    }
}