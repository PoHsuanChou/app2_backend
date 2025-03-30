package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String id;
    private String username;
    private String name;
    private String email;
    private String picture;
    private Integer age;
    private String gender;
    private List<String> interests;
    private String about;
    private String location;
}