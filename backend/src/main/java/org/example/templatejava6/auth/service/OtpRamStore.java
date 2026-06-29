package org.example.templatejava6.auth.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpRamStore {

    private static final Duration OTP_TTL = Duration.ofMinutes(15);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(30);

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    public boolean canSend(String email) {
        OtpEntry entry = store.get(normalize(email));
        if (entry == null) {
            return true;
        }
        return Duration.between(entry.lastSentAt(), Instant.now()).compareTo(RESEND_COOLDOWN) >= 0;
    }

    public void save(String email, String otp) {
        String key = normalize(email);
        store.put(key, new OtpEntry(otp, Instant.now().plus(OTP_TTL), Instant.now()));
    }

    public boolean verifyAndRemove(String email, String otp) {
        String key = normalize(email);
        OtpEntry entry = store.get(key);
        if (entry == null) {
            return false;
        }
        if (Instant.now().isAfter(entry.expiresAt())) {
            store.remove(key);
            return false;
        }
        if (!entry.otp().equals(otp.trim())) {
            return false;
        }
        store.remove(key);
        return true;
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record OtpEntry(String otp, Instant expiresAt, Instant lastSentAt) {
    }
}
