package ru.practicum.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.dto.ApiError;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildError(e.getMessage(), "The required object was not found.", HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException e) {
        String reason = e instanceof DataIntegrityViolationException
                ? "Integrity constraint has been violated."
                : "For the requested operation the conditions are not met.";
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildError(e.getMessage(), reason, HttpStatus.CONFLICT));
    }

    @ExceptionHandler({BadRequestException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception e) {
        if (e instanceof MethodArgumentNotValidException validationException) {
            List<String> errors = validationException
                    .getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(this::formatFieldError)
                    .toList();
            ApiError body = ApiError
                    .builder()
                    .errors(errors)
                    .message(errors.isEmpty() ? "Validation failed" : errors.getFirst())
                    .reason("Incorrectly made request.")
                    .status(HttpStatus.BAD_REQUEST.name())
                    .timestamp(LocalDateTime.now())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildError(e.getMessage(), "Incorrectly made request.", HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildError(e.getMessage(), "Internal server error.", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private ApiError buildError(String message, String reason, HttpStatus status) {
        return ApiError
                .builder()
                .errors(List.of())
                .message(message)
                .reason(reason)
                .status(status.name())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String formatFieldError(FieldError error) {
        return "Field: " + error.getField() + ". Error: " + error.getDefaultMessage()
                + ". Value: " + error.getRejectedValue();
    }
}
