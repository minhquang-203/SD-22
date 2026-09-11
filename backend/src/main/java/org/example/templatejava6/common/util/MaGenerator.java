package org.example.templatejava6.common.util;

import java.security.SecureRandom;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUM =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private MaGenerator() {
    }

    public static String nextCode(String prefix, Collection<String> existingCodes) {
        return nextCode(prefix, existingCodes, 2);
    }

    /** Sinh mã dạng PREFIX + số pad đủ digits (vd NCC + 4 → NCC0005, PN + 6 → PN000001). */
    public static String nextCode(String prefix, Collection<String> existingCodes, int digits) {
        int max = 0;
        Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + "(\\d+)$");
        for (String code : existingCodes) {
            if (code == null) {
                continue;
            }
            Matcher matcher = pattern.matcher(code.trim());
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        int width = Math.max(1, digits);
        return prefix + String.format("%0" + width + "d", max + 1);
    }

    /**
     * Mã đợt giảm giá: {@code SALE-YYYYMM-XXX} (XXX tăng dần trong tháng).
     */
    public static String nextMonthlySaleCode(Collection<String> existingCodes, int seqDigits) {
        return nextMonthlySaleCode(YearMonth.now(), existingCodes, seqDigits);
    }

    public static String nextMonthlySaleCode(
            YearMonth yearMonth, Collection<String> existingCodes, int seqDigits) {
        String prefix = "SALE-" + yearMonth.format(YEAR_MONTH) + "-";
        return nextCode(prefix, existingCodes, seqDigits);
    }

    /**
     * Mã phiếu giảm giá: {@code SNO-} + {@code randomLen} ký tự A–Z/0–9, kiểm tra trùng qua {@code exists}.
     */
    public static String randomVoucherCode(int randomLen, Predicate<String> exists) {
        int len = Math.max(1, randomLen);
        for (int attempt = 0; attempt < 64; attempt++) {
            StringBuilder sb = new StringBuilder("SNO-");
            for (int i = 0; i < len; i++) {
                sb.append(ALPHANUM[RANDOM.nextInt(ALPHANUM.length)]);
            }
            String code = sb.toString();
            if (!exists.test(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Không sinh được mã phiếu giảm giá duy nhất");
    }
}
