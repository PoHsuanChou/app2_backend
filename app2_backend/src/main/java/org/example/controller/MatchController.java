package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.MatchResponse;
import org.example.entity.ChatMessagePreview;
import org.example.entity.ChatRoom;
import org.example.entity.Match;
import org.example.entity.User;
import org.example.repository.ChatRoomRepository;
import org.example.repository.UserRepository;
import org.example.service.ChatMessagePreviewService;
import org.example.service.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Slf4j
public class MatchController {

    private final MatchService matchService;
    private final UserRepository userRepository;
    private final ChatMessagePreviewService chatMessagePreviewService;
    private final ChatRoomRepository chatRoomRepository;

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        Match createdMatch = matchService.createMatch(match);
        return ResponseEntity.ok(createdMatch);
    }

    @GetMapping("/findMatches")
    @Operation(
            summary = "Get matches by user ID",
            description = "Fetches the list of matches for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth") // 指定安全要求
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved matches"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user ID not found")
    })
    public ResponseEntity<List<MatchResponse>> getMatchesByUser(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId"); // ✅ 從 Filter 取得 userId
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 取得匹配紀錄
        //TODO 且沒有聊天過
        List<Match> matches = matchService.getMatchesByUserId(userId);

        // 取得對方的 userId (過濾掉自己)
        List<String> matchedUserIds = matches.stream()
                .map(m -> m.getUser1Id().equals(userId) ? m.getUser2Id() : m.getUser1Id())
                .distinct()
                .collect(Collectors.toList());

        // 查詢所有對方的用戶資訊
        List<User> matchedUsers = userRepository.findByIdIn(matchedUserIds);

        // 查詢所有聊天房間，過濾掉有聊天記錄的用戶
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContaining(userId);
        Set<String> usersWithChatHistory = new HashSet<>();

        for (ChatRoom chatRoom : chatRooms) {
            List<String> participants = chatRoom.getParticipantIds();
            // 檢查是否只有這兩個使用者在聊天房間中
            if (participants.size() == 2 && participants.contains(userId)) {
                usersWithChatHistory.add(participants.stream()
                        .filter(participantId -> !participantId.equals(userId))
                        .findFirst()
                        .orElse(null));
            }
        }


        // 建立回應列表
        List<MatchResponse> response = new ArrayList<>();

        // 先加入 "likes" 訊息
        response.add(new MatchResponse("likes", null, null, Math.max(matches.size() - 1, 0)));

        // 加入匹配對象資訊
        // 加入匹配對象資訊，排除有聊天記錄的用戶
        for (User user : matchedUsers) {
            if (!usersWithChatHistory.contains(user.getId())) {
                log.info("user:{}", user);
                response.add(new MatchResponse(
                        user.getId(),
                        user.getNickName(),
                        user.getPicture() != null ? user.getPicture() : "default.png",
                        0
                ));
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/messages")
    @Operation(
            summary = "Get chat message previews",
            description = "Fetches chat message previews for the authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth") // 指定安全要求
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved chat message previews"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, user ID not found")
    })
    public ResponseEntity<List<ChatMessagePreview>> getChatPreviews(
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        List<ChatMessagePreview> previews = chatMessagePreviewService.getChatPreviews(userId);
        return ResponseEntity.ok(previews);
    }
}
