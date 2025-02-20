package org.example.faker;

import lombok.RequiredArgsConstructor;
import org.example.MatchStatus;
import org.example.entity.Match;
import org.example.repository.MatchRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Profile("dev")  // 只在 dev 環境執行，避免污染正式環境
@RequiredArgsConstructor
public class FakerDataInitializer implements CommandLineRunner {

    private final MatchRepository matchRepository;

    @Override
    public void run(String... args) throws Exception {
        createMatches();
    }

    private void createMatches() {
        if (matchRepository.count() > 0) {
            System.out.println("已有數據，跳過初始化");
            return;
        }

        List<Match> matches = List.of(
                Match.builder()
                        .user1Id("67b5fc1a53f27d64b537651c")
                        .user2Id("67b5fc1a53f27d64b537651d")
                        .matchScore(85.0)
                        .status(MatchStatus.ACCEPTED)
                        .matchDate(new Date())
                        .lastInteractionDate(new Date())
                        .build(),

                Match.builder()
                        .user1Id("67b5fc1a53f27d64b537651c")
                        .user2Id("67b5fc1a53f27d64b537651e")
                        .matchScore(90.0)
                        .status(MatchStatus.PENDING)
                        .matchDate(new Date())
                        .lastInteractionDate(new Date())
                        .build(),

                Match.builder()
                        .user1Id("67b5fc1a53f27d64b537651c")
                        .user2Id("67b5fc1a53f27d64b537651f")
                        .matchScore(75.0)
                        .status(MatchStatus.REJECTED)
                        .matchDate(new Date())
                        .lastInteractionDate(new Date())
                        .build()
        );

        matchRepository.saveAll(matches);
        System.out.println("✅ 測試配對數據初始化完成！");

    }
}
