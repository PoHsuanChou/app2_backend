package org.example.repository;

import org.example.entity.Swipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SwipeRepository extends MongoRepository<Swipe, String> {
    List<Swipe> findByUserId(String userId); // 查找某用戶的所有 swipe 記錄
}
