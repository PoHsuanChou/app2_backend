package org.example.repository;

import org.example.entity.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByUser1IdOrUser2Id(String user1Id, String user2Id);

    @Query("{ '$or': [ { 'user1Id': ?0, 'user2Id': ?1 }, { 'user1Id': ?1, 'user2Id': ?0 } ] }")
    Match findByUserIds(String userId1, String userId2);
}
