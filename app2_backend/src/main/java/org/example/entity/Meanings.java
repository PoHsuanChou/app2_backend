package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "meanings")
@Data
@NoArgsConstructor
public class Meanings {
    private String upright;
    private String reversed;
}