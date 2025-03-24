package org.example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.MatchStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

// Match.java
@Document(collection = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(def = "{'user1Id': 1, 'user2Id': 1}", unique = true)
public class Match {
    @Id
    private String id;

    private String user1Id;
    private String user2Id;

    private String user1TarotDrawId;
    private String user2TarotDrawId;

    private Double matchScore;

    @Indexed
    private MatchStatus status;

    @Indexed
    private Date matchDate;
    private Date lastInteractionDate;

}

