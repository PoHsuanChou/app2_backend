package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.ChatRoomStatus;
import org.example.MatchStatus;
import org.example.dto.ApiResponse;
import org.example.dto.MatchResponse;
import org.example.dto.dating.DatingUserDTO;
import org.example.dto.dating.SwipeRequest;
import org.example.dto.dating.SwipeResponse;
import org.example.entity.*;
import org.example.repository.ChatRoomRepository;
import org.example.repository.MatchRepository;
import org.example.repository.SwipeRepository;
import org.example.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/dating")
@RequiredArgsConstructor
public class DatingController {
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SwipeRepository swipeRepository; // 用於檢查已 swipe 的用戶

    @Value("${backend}")
    private String backend;

    @GetMapping("/recommand")
    public ResponseEntity<?> getDatingUsers(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "User not authenticated"));
        }

        // Query current user's gender
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String currentUserGender = currentUser.getProfile() != null
                ? currentUser.getProfile().getGender()
                : null;

        if (currentUserGender == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Query all users, excluding current user
        List<User> allUsers = userRepository.findAllByIdNot(userId);

        // Query swiped users
        List<Swipe> swipes = swipeRepository.findByUserId(userId);
        Set<String> swipedUserIds = swipes.stream()
                .map(Swipe::getTargetUserId)
                .collect(Collectors.toSet());

        // Query matched users (ACCEPTED matches)
        List<Match> matches = matchRepository.findMatchesByUserId(userId);
        Set<String> matchedUserIds = matches.stream()
                .filter(match -> match.getStatus() == MatchStatus.ACCEPTED)
                .map(match -> match.getUser1Id().equals(userId) ? match.getUser2Id() : match.getUser1Id())
                .collect(Collectors.toSet());

        // Filter opposite-gender users, excluding swiped and matched users
        List<DatingUserDTO> users = allUsers.stream()
                .filter(user -> !swipedUserIds.contains(user.getId())) // Exclude swiped users
                .filter(user -> !matchedUserIds.contains(user.getId())) // Exclude matched users
                .filter(user -> {
                    String userGender = user.getProfile() != null ? user.getProfile().getGender() : null;
                    return userGender != null && !userGender.equals(currentUserGender);
                })
                .map(this::convertToDatingUserDTO)
                .limit(10) // Limit to 10 users
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @PostMapping("/swipe")
    @Transactional
    public ResponseEntity<SwipeResponse> handleSwipe(@RequestBody SwipeRequest request) {
        String userId = request.getUserId();
        String targetUserId = request.getTargetUserId();

        // Normalize user IDs: smaller ID as user1Id
        String user1Id = userId.compareTo(targetUserId) < 0 ? userId : targetUserId;
        String user2Id = userId.compareTo(targetUserId) < 0 ? targetUserId : userId;

        // Record swipe
        Swipe swipe = Swipe.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .action(request.getAction())
                .timestamp(new Date())
                .build();
        swipeRepository.save(swipe);

        if ("LIKE".equals(request.getAction())) {
            // Check for existing match with normalized IDs
            Optional<Match> existingMatch = matchRepository.findMatchByUserPair(user1Id, user2Id);

            if (existingMatch.isPresent()) {
                Match match = existingMatch.get();
                if ("PENDING".equals(match.getStatus().name())) {
                    // Match successful (reverse LIKE found)
                    match.setStatus(MatchStatus.ACCEPTED);
                    match.setMatchDate(new Date());
                    matchRepository.save(match);

                    // Create chat room
                    ChatRoom chatRoom = ChatRoom.builder()
                            .match(match)
                            .participantIds(Arrays.asList(userId, targetUserId))
                            .status(ChatRoomStatus.ACTIVE)
                            .createdAt(new Date())
                            .build();
                    chatRoomRepository.save(chatRoom);

                    return ResponseEntity.ok(new SwipeResponse(true, true, chatRoom.getId()));
                }
                // If match is already ACCEPTED or otherwise, no action needed
                return ResponseEntity.ok(new SwipeResponse(true, false, null));
            } else {
                // Create new PENDING match with normalized IDs
                Match match = Match.builder()
                        .user1Id(user1Id)
                        .user2Id(user2Id)
                        .status(MatchStatus.PENDING)
                        .matchDate(new Date())
                        .build();
                matchRepository.save(match);
            }
        }
        return ResponseEntity.ok(new SwipeResponse(true, false, null));
    }

    private DatingUserDTO convertToDatingUserDTO(User user) {
        Profile profile = user.getProfile();
        Integer age = profile != null && profile.getBirthDate() != null
                ? calculateAge(profile.getBirthDate())
                : null;

        String imageUrl = profile != null && profile.getProfileImage() != null
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("static/uploads/") // 調整為你的實際圖片路徑
                .path(profile.getProfileImage())
                .toUriString()
                : ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/images/default.jpg")  // 默認圖片的路徑
                .toUriString();

        return DatingUserDTO.builder()
                .id(user.getId())
                .name(profile != null ? profile.getName() : user.getUsername())
                .age(age)
                .bio(profile != null ? profile.getBio() : "")
                .image(imageUrl) // 返回完整 URL
                .build();
    }

    private Integer calculateAge(Date birthDate) {
        LocalDate birth = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        return Period.between(birth, now).getYears();
    }


}
