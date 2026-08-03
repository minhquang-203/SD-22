package org.example.templatejava6.common.util;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaGenerator {

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
}
