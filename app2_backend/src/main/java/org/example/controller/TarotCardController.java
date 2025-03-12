package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TarotCardResponse;
import org.example.entity.DailyTarotDraw;
import org.example.repository.DailyTarotDrawRepository;
import org.example.service.TarotCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/api/cards")
@Slf4j
@RequiredArgsConstructor
public class TarotCardController {

    private final TarotCardService tarotCardService;
    private final DailyTarotDrawRepository dailyTarotDrawRepository;
    private final Random random = new Random();


    @GetMapping("/getCard")
    @Operation(summary = "Get tarot card details by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved card details"),
            @ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<TarotCardResponse> getCardDetails(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        TarotCardResponse cardDetails = tarotCardService.getCardDetails(userId,getRandomCardId());

        DailyTarotDraw dailyTarotDraw = new DailyTarotDraw();
        dailyTarotDraw.setUserId(userId);
        dailyTarotDraw.setCardId(cardDetails.getNumber());
        dailyTarotDraw.setDate(new Date());
        dailyTarotDraw.setCreatedAt(new Date());

        // 保存到資料庫
        DailyTarotDraw savedDraw = dailyTarotDrawRepository.save(dailyTarotDraw);

        return ResponseEntity.ok(cardDetails);
    }
    public Integer getRandomCardId() {
        // 隨機生成 1 到 78 之間的數字
//        int randomIndex = random.nextInt(78) + 1; // 1 到 78
        return random.nextInt(7) + 1; // 1 到 78
    }

    @GetMapping("/check-draw")
    public ResponseEntity<Boolean> checkUserDrawToday(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        Date today = new Date(); // 獲取今天的日期
        // 設置日期範圍
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Date endOfDay = calendar.getTime();

        // 查詢今天是否有抽牌
        boolean hasDrawn = dailyTarotDrawRepository.existsByUserIdAndDateBetween(userId, startOfDay, endOfDay);
        return ResponseEntity.ok(hasDrawn);
    }
}
