package org.example.templatejava6.order.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

/** Sinh token tra cứu đơn không đoán được (256-bit). */
@Component
public class OrderTrackingTokenGenerator {

    private static final int BYTES = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] buf = new byte[BYTES];
        secureRandom.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }
}
