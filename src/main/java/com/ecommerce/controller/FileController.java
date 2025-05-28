package com.ecommerce.controller;

import com.ecommerce.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "general") String category) {
        String fileUrl = fileService.uploadFile(file, category);
        FileUploadResponse response = new FileUploadResponse(fileUrl, file.getOriginalFilename(), file.getSize());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(defaultValue = "general") String category) {
        List<FileUploadResponse> responses = fileService.uploadMultipleFiles(files, category);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/upload/product-images")
    public ResponseEntity<List<String>> uploadProductImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam Long productId) {
        List<String> imageUrls = fileService.uploadProductImages(files, productId);
        return ResponseEntity.ok(imageUrls);
    }

    @PostMapping("/upload/category-image")
    public ResponseEntity<String> uploadCategoryImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long categoryId) {
        String imageUrl = fileService.uploadCategoryImage(file, categoryId);
        return ResponseEntity.ok(imageUrl);
    }

    @PostMapping("/upload/avatar")
    public ResponseEntity<String> uploadUserAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long userId) {
        String avatarUrl = fileService.uploadUserAvatar(file, userId);
        return ResponseEntity.ok(avatarUrl);
    }

    @GetMapping("/download/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        Resource resource = fileService.loadFileAsResource(filename);

        String contentType = fileService.getContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/view/{filename:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) {
        Resource resource = fileService.loadFileAsResource(filename);

        String contentType = fileService.getContentType(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/delete/{filename:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String filename) {
        boolean deleted = fileService.deleteFile(filename);
        if (deleted) {
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/info/{filename:.+}")
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable String filename) {
        FileInfo fileInfo = fileService.getFileInfo(filename);
        return ResponseEntity.ok(fileInfo);
    }

    @PostMapping("/resize-image")
    public ResponseEntity<String> resizeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam int width,
            @RequestParam int height,
            @RequestParam(defaultValue = "false") boolean maintainAspectRatio) {
        String resizedImageUrl = fileService.resizeImage(file, width, height, maintainAspectRatio);
        return ResponseEntity.ok(resizedImageUrl);
    }

    @PostMapping("/compress-image")
    public ResponseEntity<String> compressImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "0.8") float quality) {
        String compressedImageUrl = fileService.compressImage(file, quality);
        return ResponseEntity.ok(compressedImageUrl);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listFiles(
            @RequestParam(defaultValue = "general") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<FileInfo> files = fileService.listFiles(category, page, size);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/storage-info")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        Map<String, Object> storageInfo = fileService.getStorageInfo();
        return ResponseEntity.ok(storageInfo);
    }

    @PostMapping("/validate")
    public ResponseEntity<FileValidationResponse> validateFile(@RequestParam("file") MultipartFile file) {
        FileValidationResponse validation = fileService.validateFile(file);
        return ResponseEntity.ok(validation);
    }

    // DTOs for file operations
    public static class FileUploadResponse {
        private String url;
        private String filename;
        private long size;
        private String contentType;
        private String category;

        public FileUploadResponse(String url, String filename, long size) {
            this.url = url;
            this.filename = filename;
            this.size = size;
        }

        public FileUploadResponse(String url, String filename, long size, String contentType, String category) {
            this.url = url;
            this.filename = filename;
            this.size = size;
            this.contentType = contentType;
            this.category = category;
        }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class FileInfo {
        private String filename;
        private String url;
        private long size;
        private String contentType;
        private String category;
        private String createdAt;
        private String lastModified;

        public FileInfo(String filename, String url, long size, String contentType, String category, String createdAt, String lastModified) {
            this.filename = filename;
            this.url = url;
            this.size = size;
            this.contentType = contentType;
            this.category = category;
            this.createdAt = createdAt;
            this.lastModified = lastModified;
        }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getLastModified() { return lastModified; }
        public void setLastModified(String lastModified) { this.lastModified = lastModified; }
    }

    public static class FileValidationResponse {
        private boolean valid;
        private String message;
        private String contentType;
        private long size;
        private List<String> errors;

        public FileValidationResponse(boolean valid, String message, String contentType, long size, List<String> errors) {
            this.valid = valid;
            this.message = message;
            this.contentType = contentType;
            this.size = size;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public long getSize() { return size; }
        public void setSize(long size) { this.size = size; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}