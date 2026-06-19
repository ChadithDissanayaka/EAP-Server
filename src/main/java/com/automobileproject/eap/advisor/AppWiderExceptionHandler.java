package com.automobileproject.eap.advisor;

import com.automobileproject.eap.exception.DuplicateEntryException;
import com.automobileproject.eap.exception.EntryNotFoundException;
import com.automobileproject.eap.exception.ValidationException;
import com.automobileproject.eap.util.StandardResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class AppWiderExceptionHandler {

    // ── Domain exceptions ──────────────────────────────────────────────────────

    @ExceptionHandler(EntryNotFoundException.class)
    public ResponseEntity<StandardResponseDTO> handleEntryNotFound(EntryNotFoundException ex) {
        log.warn("Entry not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                StandardResponseDTO.builder()
                        .code(404)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<StandardResponseDTO> handleDuplicateEntry(DuplicateEntryException ex) {
        log.warn("Duplicate entry: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                StandardResponseDTO.builder()
                        .code(409)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<StandardResponseDTO> handleValidation(ValidationException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                StandardResponseDTO.builder()
                        .code(400)
                        .message(ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    // ── Spring Validation (@Valid) ────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponseDTO> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        log.warn("Request validation failed: {}", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                StandardResponseDTO.builder()
                        .code(400)
                        .message("Validation failed")
                        .data(fieldErrors)
                        .build()
        );
    }

    // ── Spring Security ────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                StandardResponseDTO.builder()
                        .code(403)
                        .message("Access denied: " + ex.getMessage())
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardResponseDTO> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                StandardResponseDTO.builder()
                        .code(401)
                        .message("Invalid email or password")
                        .data(null)
                        .build()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<StandardResponseDTO> handleDisabledUser(DisabledException ex) {
        log.warn("Disabled user attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                StandardResponseDTO.builder()
                        .code(403)
                        .message("Account is disabled")
                        .data(null)
                        .build()
        );
    }

    // ── File upload ────────────────────────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<StandardResponseDTO> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex) {
        log.warn("File upload too large: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatusCode.valueOf(413))
                .body(
                        StandardResponseDTO.builder()
                                .code(413)
                                .message("File size exceeds the maximum allowed limit")
                                .data(null)
                                .build()
                );
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponseDTO> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                StandardResponseDTO.builder()
                        .code(500)
                        .message("An unexpected error occurred. Please try again later.")
                        .data(null)
                        .build()
        );
    }
}
