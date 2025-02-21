package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {
    private String id;
    private String name;
    private String image;
    private Integer count; // 只有 "likes" 會有 count 值
}
