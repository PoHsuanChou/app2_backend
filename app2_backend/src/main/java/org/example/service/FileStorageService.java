package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String fileName) throws IOException {
        try {
            System.out.println("uploadDir: " + uploadDir);
            // 確保目錄存在
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            System.out.println("uploadPath: " + uploadPath);

            // 清理文件名
            fileName = StringUtils.cleanPath(fileName);

            // 檢查文件名是否包含非法字符
            if (fileName.contains("..")) {
                throw new RuntimeException("Invalid file path sequence: " + fileName);
            }

            // 複製文件到目標位置
            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            log.error("Could not store file " + fileName, ex);
            throw new IOException("Could not store file " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting file: " + fileName, e);
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }
}
