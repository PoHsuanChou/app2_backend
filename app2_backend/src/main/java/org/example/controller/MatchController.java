package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.entity.Match;
import org.example.service.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        Match createdMatch = matchService.createMatch(match);
        return ResponseEntity.ok(createdMatch);
    }

    @GetMapping("/matches")
    public ResponseEntity<List<Match>> getMatchesByUser(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); // ✅ 從 Filter 存的值拿 userId
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Match> matches = matchService.getMatchesByUserId(userId);
        return ResponseEntity.ok(matches);
    }
}
