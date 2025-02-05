package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "locations")
@Data
@NoArgsConstructor
public class Location {
    @Id
    private String id;
    private String type = "Point";
    private List<Double> coordinates; // [longitude, latitude]
}