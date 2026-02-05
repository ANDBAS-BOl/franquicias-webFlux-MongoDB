package com.seti.franchises.infrastructure.web.exception;

import com.seti.franchises.application.service.FranchiseUseCaseService;
import com.seti.franchises.infrastructure.web.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Manejador global de excepciones para respuestas REST adecuadas (404, 400, 409).
 * Alinea con el plan: "Manejo de errores con onError y respuestas adecuadas (404, 400, 409)".
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FranchiseUseCaseService.NotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(
            FranchiseUseCaseService.NotFoundException ex,
            ServerWebExchange exchange) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message(ex.getMessage())
                        .build()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {
        log.warn("Petición inválida: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(ex.getMessage())
                        .build()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(WebExchangeBindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Error de validación");
        log.warn("Validación fallida: {}", message);
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(message)
                        .build()));
    }
}
