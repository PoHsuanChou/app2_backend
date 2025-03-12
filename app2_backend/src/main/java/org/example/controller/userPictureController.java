package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.dto.ApiResponse;
import org.example.entity.User;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.UserRepository;
import org.example.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class userPictureController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService; // 用於處理文件上傳

    @PostMapping("/profile-picture")
    public ResponseEntity<?> updateProfilePicture(
            @RequestParam("profileImage") MultipartFile file,
            HttpServletRequest request) {

        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "User not authenticated"));
        }

        try {
            // 1. 儲存文件
            String fileName = fileStorageService.storeFile(file);
            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            // 2. 更新用戶資料
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // 3. 刪除舊圖片（如果存在且不是默認圖片）
            if (user.getPicture() != null && !user.getPicture().equals("default.png")) {
                fileStorageService.deleteFile(user.getPicture());
            }

            // 4. 更新新圖片URL
            user.setPicture(fileName);
            userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse(true, "Profile picture updated successfully", fileUrl));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Could not upload profile picture: " + e.getMessage()));
        }
    }
}
