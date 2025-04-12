package org.example.repository;

import org.example.entity.DailyTarotDraw;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.Optional;

public interface DailyTarotDrawRepository extends MongoRepository<DailyTarotDraw, String> {
    // 新增方法以檢查使用者在指定日期範圍內是否有抽牌
    boolean existsByUserIdAndDateBetween(String userId, Date startDate, Date endDate);

    Optional<DailyTarotDraw> findByUserIdAndDate(String userId, Date date);

    @Query("{ 'userId': ?0, 'date': { $gte: ?1, $lt: ?2 } }")
    Optional<DailyTarotDraw> findByUserIdAndDateRange(String userId, Date startDate, Date endDate);
}
