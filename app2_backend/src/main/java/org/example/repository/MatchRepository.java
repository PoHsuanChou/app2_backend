package org.example.repository;

import org.example.entity.Match;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByUser1IdOrUser2Id(String user1Id, String user2Id);
}
