package org.example.templatejava6.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "FAILED");
        res.put("code", "INTERNAL_ERROR");
        res.put("message", ex.getMessage());
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
