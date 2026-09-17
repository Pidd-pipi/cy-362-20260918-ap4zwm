package com.generated.ldmurdergame.exception;

import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, String>> handleApiException(ApiException exception) {
    return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .filter(item -> item != null && !item.isBlank())
        .findFirst()
        .orElse("请求参数不合法");
    return ResponseEntity.badRequest().body(Map.of("message", message));
  }

  @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
      HttpMessageNotReadableException.class})
  public ResponseEntity<Map<String, String>> handleBadRequest(Exception exception) {
    return ResponseEntity.badRequest().body(Map.of("message", "请求参数不合法，请检查后重试"));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Map<String, String>> handleConflict(DuplicateKeyException exception) {
    return ResponseEntity.badRequest().body(Map.of("message", "操作冲突：请勿重复上岗或重复提交"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleUnexpected(Exception exception) {
    return ResponseEntity.internalServerError().body(Map.of("message", "服务暂时不可用，请稍后重试"));
  }
}
