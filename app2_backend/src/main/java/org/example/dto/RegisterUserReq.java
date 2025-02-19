package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class RegisterUserReq {
//    @NotBlank(message = "Email cannot be blank")
//    @Email(message = "Invalid email format")
//    private String email;
//
//    @NotBlank(message = "Gender cannot be blank")
//    private String gender;
//
//    @NotBlank(message = "Nickname cannot be blank")
//    @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters")
//    private String nickname;
//
//    @NotNull(message = "Birthday cannot be null")
//    private Date birthday;
//
//    @NotBlank(message = "Password cannot be blank")
//    @Size(min = 6, message = "Password must be at least 6 characters long")
//    private String password;
//
//    private String bio;
//    private String zodiacSign;
//    private List<String> interests;
//    private boolean isGoogleLogin;
//    private Integer profileImage;
//}
