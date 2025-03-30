package org.example.controller;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.ApiResponse;
import org.example.dto.UserProfileResponse;
import org.example.entity.Profile;
import org.example.entity.User;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.UserRepository;
import org.example.service.FileStorageService;
import org.example.service.JwtService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService; // 用於處理文件上傳
    private final JwtService jwtService;

    @PostMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfilePicture(
            @RequestPart("profileImage") MultipartFile file,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "User not authenticated"));
        }

        try {
            log.info("Received file: name={}, size={}, contentType={}",
                    file.getOriginalFilename(),
                    file.getSize(),
                    file.getContentType());

            // 檢查文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Please select a file to upload"));
            }

            // 生成文件名
            String fileExtension = ".jpg"; // 默認擴展名
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            String savedFileName = fileStorageService.storeFile(file, newFileName);

            // 更新用戶資料
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // 刪除舊圖片
            if (user.getPicture() != null && !user.getPicture().equals("default.png")) {
                fileStorageService.deleteFile(user.getPicture());
            }

            // 更新用戶圖片路徑
            user.setPicture(savedFileName);
            userRepository.save(user);

            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(savedFileName)
                    .toUriString();

            return ResponseEntity.ok(new ApiResponse(true, "Profile picture updated successfully", fileUrl));

        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Could not upload profile picture: " + e.getMessage()));
        }
    }
    // 獲取當前用戶的照片
    @GetMapping("/profile-picture/current")
    public ResponseEntity<?> getCurrentUserProfilePicture(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 从 JWT 获取用户信息
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Authorization header is missing or invalid"));
            }

            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);

            // 从数据库获取用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String fileName = user.getPicture();
            if (fileName == null) {
                fileName = "default.png";
            }

            // 构建文件URL
            // MODIFIED: Construct the URL based on your static resource handling
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/static/uploads/") // Match your Spring resource mapping
                    .path(fileName)
                    .toUriString();

            return ResponseEntity.ok(new ApiResponse(true, fileUrl));
        } catch (Exception e) {
            log.error("Error retrieving profile picture", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Could not retrieve profile picture: " + e.getMessage()));
        }
    }

    // 刪除照片
    @DeleteMapping("/profile-picture")
    public ResponseEntity<?> deleteProfilePicture(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 驗證 JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "Authorization header is missing or invalid"));
            }

            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String currentPicture = user.getPicture();

            // 檢查是否為默認圖片
            if (currentPicture == null || currentPicture.equals("default.png")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Cannot delete default profile picture"));
            }

            // 刪除文件
            fileStorageService.deleteFile(currentPicture);

            // 重置為默認圖片
            user.setPicture("default.png");
            userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse(true, "Profile picture deleted successfully"));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting profile picture", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Could not delete profile picture: " + e.getMessage()));
        }
    }
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        // 获取用户个人资料
        Profile profile = user.getProfile();
        if (profile == null) {
            return ResponseEntity.ok(UserProfileResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .picture(user.getPicture())
                    .interests(Collections.emptyList())
                    .about("")
                    .build());
        }

        // 构建头像URL
        String imageUrl = profile.getProfileImage() != null
                ? ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("static/uploads/")
                .path(profile.getProfileImage())
                .toUriString()
                : user.getPicture() != null
                ? user.getPicture()
                : ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/images/default.jpg")
                .toUriString();

        // 构建响应
        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(profile.getName())
                .email(user.getEmail())
                .picture(imageUrl)
                .age(profile.getBirthDate() != null ? calculateAge(profile.getBirthDate()) : null)
                .gender(profile.getGender())
                .interests(profile.getInterests() != null ? profile.getInterests() : Collections.emptyList())
                .about(profile.getBio())
                .build();

        return ResponseEntity.ok(response);
    }

    // 计算年龄的辅助方法
    private Integer calculateAge(Date birthDate) {
        if (birthDate == null) return null;

        LocalDate birth = birthDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate now = LocalDate.now();

        return Period.between(birth, now).getYears();
    }
}
