package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

// Match.java
@Document(collection = "matches")
@Data
@NoArgsConstructor
public class Match {
    @Id
    private String id;

    @DBRef
    private User user1;

    @DBRef
    private User user2;

    @DBRef
    private DailyTarotDraw user1TarotDraw;

    @DBRef
    private DailyTarotDraw user2TarotDraw;

    private Double matchScore;

    private MatchStatus status;
    private Date matchDate;
    private Date lastInteractionDate;

    @Data
    public static class MatchStatus {
        private String user1Status;  // "pending", "accepted", "rejected"
        private String user2Status;
    }
}

