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




    public TarotCardResponse getCardDetails(String userId, Integer cardId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        TarotCard card = tarotCardRepository.findByNumber(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        TarotCardResponse res = TarotCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .category(card.getCategory())
                .number(card.getNumber())
                .meanings(card.getMeanings())
                .elementalAffinity(card.getElementalAffinity())
                .zodiacAffinity(card.getZodiacAffinity())
                .numerologicalValue(card.getNumerologicalValue())
                .imageUrl(card.getImageUrl())
                .description(card.getDescription())
                .build();

//        // Determine the opposite gender
//        String oppositeGender = user.getProfile().getGender().equalsIgnoreCase("male") ? "female" : "male";
//
//        // Find users of the opposite gender
//        List<User> oppositeGenderUsers = userRepository.findByGender(oppositeGender);
//
//        // Randomly select 5 users
//        List<String> potentialMatchUserIds = new Random().ints(0, oppositeGenderUsers.size())
//                .distinct()
//                .limit(5)
//                .mapToObj(oppositeGenderUsers::get)
//                .map(User::getId)
//                .collect(Collectors.toList());
//
//        // Set potential match user IDs in the response
//
//        // Create and save the DailyTarotDraw
//        DailyTarotDraw dailyTarotDraw = new DailyTarotDraw();
//        dailyTarotDraw.setUserId(userId);
//        dailyTarotDraw.setCardId(card.getNumber());
//        dailyTarotDraw.setDate(new Date());
//        dailyTarotDraw.setCreatedAt(new Date());
//        dailyTarotDraw.setPotentialMatchUserIds(potentialMatchUserIds);
//
//        dailyTarotDrawRepository.save(dailyTarotDraw);

        return res;
    }
}
