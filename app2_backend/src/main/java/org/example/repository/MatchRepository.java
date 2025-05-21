package org.example.repository;

import org.example.MatchStatus;
import org.example.entity.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByUser1IdOrUser2Id(String user1Id, String user2Id);

    @Query("{ '$or': [ { 'user1Id': ?0, 'user2Id': ?1 }, { 'user1Id': ?1, 'user2Id': ?0 } ] }")
    Match findByUserIds(String userId1, String userId2);

    @Query("{ $or: [ { 'user1Id': ?0, 'user2Id': ?1 }, { 'user1Id': ?1, 'user2Id': ?0 } ] }")
    Match findByUsers(String userId, String otherUserId);

    @Query("{'$or': [{'user1Id': ?0}, {'user2Id': ?0}]}")
    List<Match> findMatchesByUserId(String userId);

    // 查找任意方向的匹配
    @Query("{$or: [{'user1Id': ?0, 'user2Id': ?1}, {'user1Id': ?1, 'user2Id': ?0}]}")
    Optional<Match> findMatchBetweenUsers(String user1Id, String user2Id);
    // 可選：檢查某個用戶的所有 PENDING 匹配
    List<Match> findByUser1IdAndStatus(String user1Id, MatchStatus status);

    @Query("{ user1Id: ?0, user2Id: ?1 }")
    Optional<Match> findMatchByUserPair(String user1Id, String user2Id);
}
