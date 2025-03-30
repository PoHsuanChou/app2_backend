package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ChatMessagePreview;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

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

        // Find chat rooms where the user is a participant and startToChat is null or "N"
        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantIdsContainingAndStartToChatIsNullOrStartToChatEquals(
                userId, "N");

        // Build the response list
        List<MatchResponse> responseList = new ArrayList<>();

        // Add the first special "likes" entry with count of matches
        responseList.add(new MatchResponse("likes", null, null, null, Math.max(chatRooms.size() - 1, 0)));

        for (ChatRoom chatRoom : chatRooms) {
            // Find the other participant's ID (not the current user)
            String otherUserId = chatRoom.getParticipantIds().stream()
                    .filter(id -> !id.equals(userId))
                    .findFirst()
                    .orElse(null);

            if (otherUserId != null) {
                // Get other user's information
                User otherUser = userRepository.findById(otherUserId).orElse(null);

                if (otherUser != null) {


                    // Build image URL
                    String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path("static/uploads/")
                            .path(otherUser.getPicture())
                            .toUriString();

                    // Create response object
                    MatchResponse matchResponse = new MatchResponse(
                            otherUserId,
                            otherUser.getUsername(),
                            imageUrl,
                            chatRoom.getId(),
                            null // count is null for regular match entries
                    );

                    responseList.add(matchResponse);
                }
            }
        }

        return ResponseEntity.ok(responseList);
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
