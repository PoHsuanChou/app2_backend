package org.example.dto.dating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatingUserDTO {
    private String id;
    private String name;
    private Integer age;
    private String bio;
    private String image;
}
