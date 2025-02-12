package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.RegisterUserReq;
import org.example.entity.User;
import org.example.exception.UserNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/login")
@RequiredArgsConstructor
@Slf4j
public class loginController {

    @PostMapping("register")
    public List<User> getAllUsers(@Valid @RequestBody RegisterUserReq registerUserReq ) {
        log.info("enter registUser and request is registerUserReq={}",registerUserReq);



            throw new UserNotFoundException("User not found");
//        return null;
    }
}
