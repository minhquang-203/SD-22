package org.example.templatejava6.common.security;

import org.example.templatejava6.common.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** ID nhân viên từ JWT subject (đăng nhập admin/POS). */
    public static Integer currentNhanVienId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
        try {
            return Integer.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ApiException("Phiên đăng nhập không hợp lệ", "UNAUTHORIZED");
        }
    }
}
