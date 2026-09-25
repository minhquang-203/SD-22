package org.example.templatejava6.order.service;

import org.example.templatejava6.common.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Giới hạn số lần tra cứu đơn công khai theo IP — chống brute-force mã/email/token.
 * Key phải là IP kết nối thật ({@code remoteAddr}), không dùng X-Forwarded-For giả được.
 */
@Service
public class PublicOrderLookupRateLimiter {

    private final int maxAttempts;
    private final Duration window;
    private static final Duration CLEANUP_INTERVAL = Duration.ofMinutes(5);

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private volatile Instant lastCleanup = Instant.now();

    public PublicOrderLookupRateLimiter(
            @Value("${app.public-lookup.rate-limit.max-attempts:60}") int maxAttempts,
            @Value("${app.public-lookup.rate-limit.window-minutes:15}") int windowMinutes) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.window = Duration.ofMinutes(Math.max(1, windowMinutes));
    }

    public void checkOrThrow(String clientKey) {
        cleanupIfNeeded();
        String key = clientKey == null || clientKey.isBlank() ? "unknown" : clientKey.trim();
        Instant now = Instant.now();
        Window windowState = windows.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.resetAt())) {
                return new Window(now.plus(window), new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        if (windowState.count().get() > maxAttempts) {
            throw new ApiException(
                    "Bạn đã tra cứu quá nhiều lần. Vui lòng thử lại sau ít phút.",
                    "RATE_LIMITED");
        }
    }

    private void cleanupIfNeeded() {
        Instant now = Instant.now();
        if (Duration.between(lastCleanup, now).compareTo(CLEANUP_INTERVAL) < 0) {
            return;
        }
        lastCleanup = now;
        Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Window> e = it.next();
            if (now.isAfter(e.getValue().resetAt())) {
                it.remove();
            }
        }
    }

    private record Window(Instant resetAt, AtomicInteger count) {
    }
}
