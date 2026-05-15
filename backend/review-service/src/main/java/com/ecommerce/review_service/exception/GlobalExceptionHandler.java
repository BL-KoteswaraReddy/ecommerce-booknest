package com.ecommerce.review_service.exception;

import com.ecommerce.review_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleReviewNotFound(ReviewNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateReview(DuplicateReviewException ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(UnverifiedPurchaseException.class)
    public ResponseEntity<ApiResponse<?>> handleUnverifiedPurchase(UnverifiedPurchaseException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(ReviewServiceException.class)
    public ResponseEntity<ApiResponse<?>> handleReviewService(ReviewServiceException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, "Validation failed", errors));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>(false, "An error occurred: " + ex.getMessage(), null));
    }
}