package org.example.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RegisterUserReq;
import org.example.entity.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    @PostMapping("registerUser")
    public List<User> getAllUsers(@RequestBody RegisterUserReq registerUserReq ) {
        log.info("enter registUser and request is registerUserReq={}",registerUserReq);
        return null;
    }
}
