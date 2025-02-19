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

import java.util.Date;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<GoogleSSOResponse> getAllUsers(@Valid @RequestBody UserRegisterRequest registerUserReq ) {
        log.info("Entering registerUser with request: {}", registerUserReq);
        return userService.registerUser(registerUserReq);
    }

}