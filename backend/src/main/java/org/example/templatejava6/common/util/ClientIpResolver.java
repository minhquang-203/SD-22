package org.example.templatejava6.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Lấy IP client. Với rate-limit công khai: chỉ dùng {@link HttpServletRequest#getRemoteAddr()}
 * — không tin {@code X-Forwarded-For} từ client (header dễ giả, bypass hạn mức).
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    /**
     * Key rate-limit: IP kết nối TCP thật tới server (hoặc IP proxy trực tiếp nếu đứng sau reverse proxy
     * đã terminate TLS). Không đọc X-Forwarded-For.
     */
    public static String forRateLimit(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String remote = request.getRemoteAddr();
        return remote == null || remote.isBlank() ? "unknown" : remote.trim();
    }
}
