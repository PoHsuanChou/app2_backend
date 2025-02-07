package org.example.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Birthday {
    private String day;
    private String month;
    private String year;
    private String zodiacSign;
}
