package org.example.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.TarotCardResponse;
import org.example.entity.DailyTarotDraw;
import org.example.entity.TarotCard;
import org.example.entity.User;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.DailyTarotDrawRepository;
import org.example.repository.TarotCardRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TarotCardService {

    private final TarotCardRepository tarotCardRepository;

    private final DailyTarotDrawRepository dailyTarotDrawRepository;

    private final UserRepository userRepository;




    public TarotCardResponse getCardDetails(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        TarotCard card = getRandomTarotCard();

        TarotCardResponse res = TarotCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .category(card.getCategory())
                .number(card.getNumber())
                .meanings(card.getMeanings())
                .elementalAffinity(card.getElementalAffinity())
                .zodiacAffinity(card.getZodiacAffinity())
                .numerologicalValue(card.getNumerologicalValue())
                .imageUrl(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("static/uploads/")
                        .path(card.getImageUrl())
                        .toUriString())
                .description(card.getDescription())
                .build();


        return res;
    }
    public TarotCard getRandomTarotCard() {
        // 獲取所有塔羅牌
        List<TarotCard> allCards = tarotCardRepository.findAll();

        // 如果資料庫為空，拋出異常
        if (allCards.isEmpty()) {
            throw new ResourceNotFoundException("No tarot cards found in the database.");
        }

        // 使用 Random 隨機選取一張牌
        Random random = new Random();
        int randomIndex = random.nextInt(allCards.size());

        // 返回隨機選中的牌
        return allCards.get(randomIndex);
    }
}
