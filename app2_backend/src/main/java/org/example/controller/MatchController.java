package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.MatchResponse;
import org.example.entity.*;
import org.example.repository.ChatMessageRepository;
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
    private final ChatMessageRepository chatMessageRepository;

    @PostMapping
    public ResponseEntity<Match> createMatch(@RequestBody Match match) {
        Match createdMatch = matchService.createMatch(match);
        return ResponseEntity.ok(createdMatch);
    }

    @GetMapping("/findMatches")
    @Operation(
            summary = "Get matches by user ID",
            description = "Fetches the list of matches for the authenticated user who haven't chatted yet.",
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
        List<Match> matches = matchService.getMatchesByUserId(userId);

        // 取得對方的 userId (過濾掉自己)
        List<String> matchedUserIds = matches.stream()
                .map(m -> m.getUser1Id().equals(userId) ? m.getUser2Id() : m.getUser1Id())
                .distinct()
                .collect(Collectors.toList());

        // 查詢所有與當前用戶相關的聊天室
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContaining(userId);

        // 查詢所有已發送過消息的聊天室ID
        List<String> chatRoomIdsWithMessages = chatMessageRepository.findBySenderId(userId)
                .stream()
                .map(ChatMessage::getChatRoomId)
                .distinct()
                .collect(Collectors.toList());

        // 也查詢接收過消息的聊天室ID
        chatRoomIdsWithMessages.addAll(
                chatMessageRepository.findByReceiverId(userId)
                        .stream()
                        .map(ChatMessage::getChatRoomId)
                        .distinct()
                        .collect(Collectors.toList())
        );

        // 找出已聊天過的用戶ID
        Set<String> usersWithChatHistory = new HashSet<>();

        for (ChatRoom chatRoom : chatRooms) {
            // 只檢查有消息記錄的聊天室
            if (chatRoomIdsWithMessages.contains(chatRoom.getId())) {
                List<String> participants = chatRoom.getParticipantIds();
                // 檢查是否只有兩個用戶的聊天室
                if (participants.size() == 2 && participants.contains(userId)) {
                    // 添加對方ID到已聊天列表
                    participants.stream()
                            .filter(id -> !id.equals(userId))
                            .findFirst()
                            .ifPresent(usersWithChatHistory::add);
                }
            }
        }

        // 查詢所有對方的用戶資訊
        List<User> matchedUsers = userRepository.findByIdIn(matchedUserIds);

        // 建立回應列表
        List<MatchResponse> response = new ArrayList<>();

        // 先加入 "likes" 訊息
        response.add(new MatchResponse("likes", null, null, Math.max(matches.size() - 1, 0)));

        // 加入匹配對象資訊，排除有聊天記錄的用戶
        for (User user : matchedUsers) {
            if (!usersWithChatHistory.contains(user.getId())) {
                log.info("發現未聊天的匹配用戶: {}", user.getNickName());
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
