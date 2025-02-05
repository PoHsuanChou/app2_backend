package org.example.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "profiles")
@Data
@NoArgsConstructor
public class Profile {
    @Id
    private String id;
    private String name;
    private Date birthDate;
    private String gender;
    private List<String> photos;
    private String bio;
    private List<String> interests;
}