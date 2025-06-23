package fun.wswj.wrench.handler;

import fun.wswj.wrench.idempotent.lock.types.exception.IdempotentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<Map<String, Object>> handleIdempotentException(IdempotentException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getMessage());
        response.put("errorCode", "IDEMPOTENT_LOCK_FAILED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 可以处理其他异常
}
