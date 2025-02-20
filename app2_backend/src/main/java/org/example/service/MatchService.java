package org.example.service;
import lombok.RequiredArgsConstructor;
import org.example.entity.Match;
import org.example.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;

    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    public List<Match> getMatchesByUserId(String userId) {
        return matchRepository.findByUser1IdOrUser2Id(userId, userId);
    }
}
