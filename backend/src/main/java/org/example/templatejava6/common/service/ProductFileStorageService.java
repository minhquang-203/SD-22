package org.example.templatejava6.common.service;

import org.example.templatejava6.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductFileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    @Value("${app.upload.products-dir:uploads/products}")
    private String productsDir;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File ảnh không hợp lệ", "VALIDATION_ERROR");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ApiException("Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF", "VALIDATION_ERROR");
        }

        try {
            Path dir = Paths.get(productsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String original = file.getOriginalFilename();
            String safeName = sanitizeFilename(original);
            String storedName = UUID.randomUUID() + "_" + safeName;
            Path target = dir.resolve(storedName);
            file.transferTo(target.toFile());

            return "/uploads/products/" + storedName;
        } catch (IOException ex) {
            throw new ApiException("Không thể lưu file ảnh", "STORAGE_ERROR");
        }
    }

    private String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "image.jpg";
        }
        String name = Paths.get(original).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");
        return name.isBlank() ? "image.jpg" : name;
    }
}
