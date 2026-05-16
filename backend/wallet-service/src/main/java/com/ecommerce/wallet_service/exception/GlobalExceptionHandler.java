package com.ecommerce.wallet_service.exception;

import com.ecommerce.wallet_service.dto.ApiResponse;
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
    
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(WalletLimitExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleWalletLimitExceeded(WalletLimitExceededException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidAmount(InvalidAmountException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(WalletTransactionException.class)
    public ResponseEntity<ApiResponse<?>> handleWalletTransaction(WalletTransactionException ex) {
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
        ex.printStackTrace();
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>(false, "An error occurred: " + ex.getMessage(), null));
    }
}