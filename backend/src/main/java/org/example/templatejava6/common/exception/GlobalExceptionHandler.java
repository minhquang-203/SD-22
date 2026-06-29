package org.example.templatejava6.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_VALUE = Pattern.compile(
            "duplicate key value is \\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", "VALIDATION_ERROR");
        res.put("errors", errors);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApi(ApiException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", ex.getCode());
        res.put("message", ex.getMessage());
        HttpStatus status = "FORBIDDEN".equals(ex.getCode())
                ? HttpStatus.FORBIDDEN
                : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(res, status);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", ex.getCode());
        res.put("message", ex.getMessage());
        return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", "FORBIDDEN");
        res.put("message", "Bạn không có quyền truy cập tài nguyên này");
        return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", "DUPLICATE");
        res.put("message", toFriendlyDbMessage(ex));
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", "INTERNAL_ERROR");
        res.put("message", toFriendlyDbMessage(ex));
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    static String toFriendlyDbMessage(Throwable ex) {
        String raw = extractRootMessage(ex);
        if (raw == null) {
            return "Có lỗi xảy ra. Vui lòng thử lại.";
        }

        String lower = raw.toLowerCase();
        if (lower.contains("unique key") || lower.contains("duplicate key")) {
            Matcher matcher = DUPLICATE_VALUE.matcher(raw);
            if (matcher.find()) {
                return "Mã \"" + matcher.group(1) + "\" đã tồn tại. Vui lòng dùng mã khác.";
            }
            return "Mã đã tồn tại. Vui lòng kiểm tra lại.";
        }

        if (lower.contains("foreign key") || lower.contains("reference constraint")) {
            return "Không thể thực hiện vì dữ liệu đang được sử dụng ở nơi khác.";
        }

        if (raw.contains("insert into") || raw.contains("SQL [")) {
            return "Không thể lưu dữ liệu. Vui lòng kiểm tra lại thông tin.";
        }

        return raw;
    }

    private static String extractRootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
