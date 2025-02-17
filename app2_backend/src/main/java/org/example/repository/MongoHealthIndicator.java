package org.example.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoHealthIndicator {
    
    private final MongoTemplate mongoTemplate;

    public void health() {
        mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
    }
} 