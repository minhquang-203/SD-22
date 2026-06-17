package org.example.templatejava6.common.util;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MaGenerator {

    private MaGenerator() {
    }

    public static String nextCode(String prefix, Collection<String> existingCodes) {
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
        return prefix + String.format("%02d", max + 1);
    }
}
