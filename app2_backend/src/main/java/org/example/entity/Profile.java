package org.example.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "profiles")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    private String id;
    private String name;
    private String bio;
    private Date birthDate;
    private String zodiacSign;
    private String gender;
    private List<String> interests;
    private Integer profileImage;
}