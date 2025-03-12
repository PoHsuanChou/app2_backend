package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "dailyTarotDraws")
@Data
@NoArgsConstructor
public class DailyTarotDraw {
    @Id
    private String id;

    private String userId;  // 只存儲 ID，不使用 DBRef

    @Indexed
    private Date date;

    private Integer cardId;

    private List<String> potentialMatchUserIds; // 只存儲 ID

    @Indexed
    private Date createdAt;
}