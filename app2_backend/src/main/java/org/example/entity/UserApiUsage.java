package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "userApiUsage")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApiUsage {
    @Id
    private String userId;           // 用戶 ID，作為主鍵
    private int geminiApiCalls;      // Gemini API 使用次數
    private LocalDate lastResetDate; // 最後重置次數的日期
}
