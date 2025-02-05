package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "matchingAttributes")
@Data
@NoArgsConstructor
public class MatchingAttributes {
    @Id
    private String id;
    private String elementalAffinity;
    private List<String> zodiacAffinity;
    private Integer numerologicalValue;
}