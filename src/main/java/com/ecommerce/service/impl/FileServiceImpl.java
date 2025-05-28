package com.ecommerce.service.impl;

import com.ecommerce.controller.FileController.FileInfo;
import com.ecommerce.controller.FileController.FileUploadResponse;
import com.ecommerce.controller.FileController.FileValidationResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileServiceImpl implements FileService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.max.size:10485760}") // 10MB default
    private long maxFileSize;

    private final String[] allowedImageTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    private final String[] allowedDocumentTypes = {"application/pdf", "text/plain", "application/msword"};

    @Override
    public String uploadFile(MultipartFile file, String category) {
        validateFile(file);

        try {
            // Create directory if it doesn't exist
            Path categoryPath = Paths.get(uploadDir, category);
            Files.createDirectories(categoryPath);

            // Generate unique filename
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            Path targetPath = categoryPath.resolve(uniqueFilename);

            // Save file
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/" + category + "/" + uniqueFilename;
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public List<FileUploadResponse> uploadMultipleFiles(MultipartFile[] files, String category) {
        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String fileUrl = uploadFile(file, category);
                FileUploadResponse response = new FileUploadResponse(
                        fileUrl,
                        file.getOriginalFilename(),
                        file.getSize(),
                        file.getContentType(),
                        category
                );
                responses.add(response);
            } catch (Exception e) {
                // Log error but continue with other files
                System.err.println("Failed to upload file " + file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return responses;
    }

    @Override
    public List<String> uploadProductImages(MultipartFile[] files, Long productId) {
        List<String> imageUrls = new ArrayList<>();
        String category = "products/" + productId;

        for (MultipartFile file : files) {
            if (!isValidImageFile(file)) {
                throw new BadRequestException("Invalid image file: " + file.getOriginalFilename());
            }

            String imageUrl = uploadFile(file, category);
            imageUrls.add(imageUrl);
        }

        return imageUrls;
    }

    @Override
    public String uploadCategoryImage(MultipartFile file, Long categoryId) {
        if (!isValidImageFile(file)) {
            throw new BadRequestException("Invalid image file");
        }

        return uploadFile(file, "categories/" + categoryId);
    }

    @Override
    public String uploadUserAvatar(MultipartFile file, Long userId) {
        if (!isValidImageFile(file)) {
            throw new BadRequestException("Invalid image file");
        }

        return uploadFile(file, "avatars/" + userId);
    }

    @Override
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BadRequestException("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new BadRequestException("Invalid file path: " + filename);
        }
    }

    @Override
    public String getContentType(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            return "application/octet-stream"; // Default content type
        }
    }

    @Override
    public FileInfo getFileInfo(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);

            if (!Files.exists(filePath)) {
                throw new BadRequestException("File not found: " + filename);
            }

            long size = Files.size(filePath);
            String contentType = Files.probeContentType(filePath);
            String createdAt = Files.getLastModifiedTime(filePath).toString();
            String lastModified = Files.getLastModifiedTime(filePath).toString();
            String url = "/" + filename;

            // Extract category from path
            String category = filename.contains("/") ?
                    filename.substring(0, filename.lastIndexOf("/")) : "general";

            return new FileInfo(filename, url, size, contentType, category, createdAt, lastModified);
        } catch (IOException e) {
            throw new BadRequestException("Failed to get file info: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<FileInfo> listFiles(String category, int page, int size) {
        List<FileInfo> files = new ArrayList<>();

        try {
            Path categoryPath = Paths.get(uploadDir, category);

            if (!Files.exists(categoryPath)) {
                return files;
            }

            Files.walk(categoryPath, 1)
                    .filter(Files::isRegularFile)
                    .skip(page * size)
                    .limit(size)
                    .forEach(path -> {
                        try {
                            String relativePath = Paths.get(uploadDir).relativize(path).toString();
                            FileInfo fileInfo = getFileInfo(relativePath);
                            files.add(fileInfo);
                        } catch (Exception e) {
                            System.err.println("Error processing file: " + e.getMessage());
                        }
                    });

        } catch (IOException e) {
            throw new BadRequestException("Failed to list files: " + e.getMessage());
        }

        return files;
    }

    @Override
    public Map<String, Object> getStorageInfo() {
        Map<String, Object> info = new HashMap<>();

        try {
            Path uploadPath = Paths.get(uploadDir);

            // Calculate total size
            long totalSize = Files.walk(uploadPath)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();

            // Count total files
            long totalFiles = Files.walk(uploadPath)
                    .filter(Files::isRegularFile)
                    .count();

            info.put("totalSize", totalSize);
            info.put("totalFiles", totalFiles);
            info.put("uploadDirectory", uploadDir);
            info.put("maxFileSize", maxFileSize);

        } catch (IOException e) {
            info.put("error", "Failed to calculate storage info");
        }

        return info;
    }

    @Override
    public FileValidationResponse validateFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file.isEmpty()) {
            errors.add("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            errors.add("File size exceeds maximum limit of " + maxFileSize + " bytes");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            errors.add("Unable to determine file type");
        }

        boolean isValid = errors.isEmpty();
        String message = isValid ? "File is valid" : "File validation failed";

        return new FileValidationResponse(isValid, message, contentType, file.getSize(), errors);
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return isValidFileType(file, allowedImageTypes);
    }

    @Override
    public boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= maxFileSize;
    }

    @Override
    public boolean isValidFileType(MultipartFile file, String[] allowedTypes) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        for (String allowedType : allowedTypes) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String resizeImage(MultipartFile file, int width, int height, boolean maintainAspectRatio) {
        // Implementation would require image processing library like ImageIO or imgscalr
        // For now, just upload the original file
        return uploadFile(file, "resized");
    }

    @Override
    public String compressImage(MultipartFile file, float quality) {
        // Implementation would require image compression
        // For now, just upload the original file
        return uploadFile(file, "compressed");
    }

    @Override
    public String generateThumbnail(MultipartFile file, int size) {
        // Implementation would require thumbnail generation
        // For now, just upload the original file
        return uploadFile(file, "thumbnails");
    }

    @Override
    public String generateUniqueFilename(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFilename);
        String nameWithoutExtension = originalFilename.substring(0,
                originalFilename.lastIndexOf('.') > 0 ? originalFilename.lastIndexOf('.') : originalFilename.length());

        return nameWithoutExtension + "_" + timestamp + extension;
    }

    @Override
    public String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    @Override
    public long getFileSize(String filename) {
        try {
            Path filePath = Paths.get(uploadDir, filename);
            return Files.size(filePath);
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public void cleanupTempFiles() {
        // Implementation for cleaning up temporary files
        try {
            Path tempPath = Paths.get(uploadDir, "temp");
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                System.err.println("Failed to delete temp file: " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Failed to cleanup temp files: " + e.getMessage());
        }
    }

    @Override
    public void deleteExpiredFiles() {
        // Implementation for deleting expired files
        // This would typically check file creation dates and delete old files
    }

    @Override
    public void optimizeStorage() {
        // Implementation for storage optimization
        // This could include compression, deduplication, etc.
    }
}