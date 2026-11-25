package com.saga.order.application.rest;

import com.saga.order.domain.exception.OrderDomainException;
import com.saga.order.domain.exception.OrderNotFoundException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.builder()
                .code("ORDER_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<ErrorResponse> handleOrderDomainException(OrderDomainException ex) {
        log.error("Order domain exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder()
                .code("ORDER_DOMAIN_ERROR")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(message)
                .timestamp(Instant.now())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .timestamp(Instant.now())
                .build());
    }

    @Schema(description = "Corpo de resposta para erros HTTP (4xx e 5xx)")
    @Getter
    @Builder
    public static class ErrorResponse {

        @Schema(
            description = "Código de erro identificando o tipo de falha",
            allowableValues = {"VALIDATION_ERROR", "ORDER_DOMAIN_ERROR", "ORDER_NOT_FOUND", "INTERNAL_ERROR"},
            example = "VALIDATION_ERROR"
        )
        private String code;

        @Schema(
            description = "Descrição legível do erro",
            example = "userId: userId is required"
        )
        private String message;

        @Schema(
            description = "Momento em que o erro ocorreu (UTC)",
            example = "2024-11-25T10:30:00Z"
        )
        private Instant timestamp;
    }
}
