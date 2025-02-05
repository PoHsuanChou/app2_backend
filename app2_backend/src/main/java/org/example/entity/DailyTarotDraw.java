package org.example.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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

    @DBRef
    private User user;

    private Date date;

    @DBRef
    private TarotCard card;

    @DBRef
    private MatchingAttributes matchingAttributes;

    @DBRef
    private List<User> potentialMatches;

    private Date createdAt;
}