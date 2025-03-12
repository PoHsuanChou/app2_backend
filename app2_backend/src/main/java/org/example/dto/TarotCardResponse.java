package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.Meanings;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarotCardResponse {
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
