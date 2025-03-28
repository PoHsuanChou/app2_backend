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

        // 查詢當前用戶的性別
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String currentUserGender = currentUser.getProfile() != null
                ? currentUser.getProfile().getGender()
                : null;

        if (currentUserGender == null) {
            // 如果當前用戶沒有設置性別，返回空列表或拋出異常，根據需求決定
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 查詢所有用戶，排除當前用戶
        List<User> allUsers = userRepository.findAllByIdNot(userId);

        // 查詢當前用戶已 swipe 的目標用戶
        List<Swipe> swipes = swipeRepository.findByUserId(userId);
        Set<String> swipedUserIds = swipes.stream()
                .map(Swipe::getTargetUserId)
                .collect(Collectors.toSet());

        // 過濾異性用戶並轉換為 DTO
        List<DatingUserDTO> users = allUsers.stream()
                .filter(user -> !swipedUserIds.contains(user.getId())) // 排除已 swipe 的用戶
                .filter(user -> {
                    String userGender = user.getProfile() != null ? user.getProfile().getGender() : null;
                    // 過濾異性：如果當前用戶是 MALE，則只保留 FEMALE，反之亦然
                    return userGender != null && !userGender.equals(currentUserGender);
                })
                .map(this::convertToDatingUserDTO)
                .limit(10) // 限制返回數量，例如 10 個
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    @PostMapping("/swipe")
    public ResponseEntity<SwipeResponse> handleSwipe(@RequestBody SwipeRequest request) {
        // 記錄 swipe
        Swipe swipe = Swipe.builder()
                .userId(request.getUserId())
                .targetUserId(request.getTargetUserId())
                .action(request.getAction())
                .timestamp(new Date())
                .build();
        swipeRepository.save(swipe);

        if ("LIKE".equals(request.getAction())) {
            // 檢查是否雙向匹配
            Optional<Match> reverseMatch = matchRepository.findMatchBetweenUsers(
                    request.getTargetUserId(), request.getUserId());

            if (reverseMatch.isPresent() && "PENDING".equals(reverseMatch.get().getStatus().name())) {
                // 匹配成功
                Match match = reverseMatch.get();
                match.setStatus(MatchStatus.ACCEPTED);
                match.setMatchDate(new Date());
                matchRepository.save(match);

                // 創建聊天室
                ChatRoom chatRoom = ChatRoom.builder()
                        .match(match)
                        .participantIds(Arrays.asList(request.getUserId(), request.getTargetUserId()))
                        .status(ChatRoomStatus.ACTIVE)
                        .createdAt(new Date())
                        .build();
                chatRoomRepository.save(chatRoom);

                return ResponseEntity.ok(new SwipeResponse(true, true, chatRoom.getId()));
            } else {
                // 單向 LIKE，創建 PENDING 匹配
                Match match = Match.builder()
                        .user1Id(request.getUserId())
                        .user2Id(request.getTargetUserId())
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
