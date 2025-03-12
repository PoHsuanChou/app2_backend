package org.example.repository;

import org.example.entity.DailyTarotDraw;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;

public interface DailyTarotDrawRepository extends MongoRepository<DailyTarotDraw, String> {
    // 新增方法以檢查使用者在指定日期範圍內是否有抽牌
    boolean existsByUserIdAndDateBetween(String userId, Date startDate, Date endDate);
}
