package com.ecommerce.service;

import com.ecommerce.controller.FileController.FileInfo;
import com.ecommerce.controller.FileController.FileUploadResponse;
import com.ecommerce.controller.FileController.FileValidationResponse;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
@Service
public interface FileService {

    // File upload operations
    String uploadFile(MultipartFile file, String category);
    List<FileUploadResponse> uploadMultipleFiles(MultipartFile[] files, String category);

    // Specific upload operations
    List<String> uploadProductImages(MultipartFile[] files, Long productId);
    String uploadCategoryImage(MultipartFile file, Long categoryId);
    String uploadUserAvatar(MultipartFile file, Long userId);

    // File retrieval
    Resource loadFileAsResource(String filename);
    String getContentType(String filename);
    FileInfo getFileInfo(String filename);

    // File management
    boolean deleteFile(String filename);
    List<FileInfo> listFiles(String category, int page, int size);
    Map<String, Object> getStorageInfo();

    // File validation
    FileValidationResponse validateFile(MultipartFile file);
    boolean isValidImageFile(MultipartFile file);
    boolean isValidFileSize(MultipartFile file);
    boolean isValidFileType(MultipartFile file, String[] allowedTypes);

    // Image processing
    String resizeImage(MultipartFile file, int width, int height, boolean maintainAspectRatio);
    String compressImage(MultipartFile file, float quality);
    String generateThumbnail(MultipartFile file, int size);

    // File utilities
    String generateUniqueFilename(String originalFilename);
    String getFileExtension(String filename);
    long getFileSize(String filename);

    // Cleanup operations
    void cleanupTempFiles();
    void deleteExpiredFiles();
    void optimizeStorage();
}