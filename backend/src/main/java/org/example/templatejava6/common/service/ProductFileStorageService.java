package org.example.templatejava6.common.service;

import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.product.ProductImageLimits;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductFileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    @Value("${app.upload.products-dir:uploads/products}")
    private String productsDir;

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("File ảnh không hợp lệ", "VALIDATION_ERROR");
        }

        if (file.getSize() > ProductImageLimits.MAX_BYTES) {
            throw new ApiException(
                    "Ảnh quá lớn, mỗi ảnh tối đa " + ProductImageLimits.MAX_FILE_SIZE_LABEL,
                    "VALIDATION_ERROR");
        }

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase(Locale.ROOT).trim()
                : "";
        String ext = resolveExtension(file.getOriginalFilename(), contentType);
        if (ext == null) {
            throw new ApiException(
                    "Định dạng ảnh không hợp lệ. Chỉ chấp nhận JPG, JPEG, PNG, WEBP",
                    "VALIDATION_ERROR");
        }
        if (!contentType.isBlank() && !ALLOWED_CONTENT_TYPES.contains(contentType)
                && !"application/octet-stream".equals(contentType)) {
            throw new ApiException(
                    "Định dạng ảnh không hợp lệ. Chỉ chấp nhận JPG, JPEG, PNG, WEBP",
                    "VALIDATION_ERROR");
        }

        try {
            Path dir = Paths.get(productsDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            if (!Files.isWritable(dir)) {
                throw new ApiException("Thư mục lưu ảnh không ghi được: " + dir, "STORAGE_ERROR");
            }

            // Tên lưu: UUID + đuôi — không dấu, không khoảng trắng, không trùng
            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(storedName).normalize();
            if (!target.startsWith(dir)) {
                throw new ApiException("Tên file ảnh không hợp lệ", "VALIDATION_ERROR");
            }
            file.transferTo(target);

            return "/uploads/products/" + storedName;
        } catch (ApiException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ApiException("Không thể lưu file ảnh. Kiểm tra thư mục uploads/products.", "STORAGE_ERROR");
        }
    }

    private String resolveExtension(String original, String contentType) {
        String fromName = null;
        if (original != null && !original.isBlank()) {
            String name = Paths.get(original).getFileName().toString().toLowerCase(Locale.ROOT);
            int dot = name.lastIndexOf('.');
            if (dot >= 0 && dot < name.length() - 1) {
                fromName = name.substring(dot + 1);
            }
        }
        if (fromName != null && ALLOWED_EXT.contains(fromName)) {
            return "jpeg".equals(fromName) ? "jpg" : fromName;
        }
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> null;
        };
    }
}
