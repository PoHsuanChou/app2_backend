package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "tarotCards")
@Data
@NoArgsConstructor
public class TarotCard {
    @Id
    private String id;
    private String name;
    private String category;
    private String suit;
    private Integer number;
    private Meanings meanings;
    private String elementalAffinity;
    private List<String> zodiacAffinity;
    private Integer numerologicalValue;
    private String imageUrl;
    private String description;

}
