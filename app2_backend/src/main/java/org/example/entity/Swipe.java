package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "swipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Swipe {
    @Id
    private String id;
    private String userId;       // 誰發起的 swipe
    private String targetUserId; // 被 swipe 的用戶
    private String action;       // "LIKE" 或 "DISLIKE"
    @Indexed
    private Date timestamp;
}
