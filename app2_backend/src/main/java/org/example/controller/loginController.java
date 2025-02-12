package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RegisterUserReq;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/login")
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
}
