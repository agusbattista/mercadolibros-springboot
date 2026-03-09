package io.github.agusbattista.mercadolibros_springboot.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private static final String INTERNAL_SERVER_ERROR_MESSAGE =
      "Ocurrió un error interno inesperado. Por favor contacte al soporte";

  // Recurso no encontrado (404)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    log.warn("Recurso no encontrado: {}", ex.getMessage());
    return this.buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // Duplicado (409)
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateResource(
      DuplicateResourceException ex) {
    log.warn("Recurso duplicado: {}", ex.getMessage());
    return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  // Recurso en uso (409)
  @ExceptionHandler(ResourceInUseException.class)
  public ResponseEntity<Map<String, Object>> handleResourceInUse(ResourceInUseException ex) {
    log.warn("Recurso en uso: {}", ex.getMessage());
    return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  // Argumentos ilegales (400)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Argumento/s ilegal/es: {}", ex.getMessage());
    return this.buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  // Bad Request (400) - Cuerpo de la petición vacío o inválido
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMalformedJson(
      HttpMessageNotReadableException ex) {
    log.warn("JSON inválido recibido: {}", ex.getMessage());
    return this.buildResponse(
        HttpStatus.BAD_REQUEST, "El cuerpo de la petición está vacío o es inválido");
  }

  /*
   * Error de validación (400) - @Valid en el controller.
   * Extrae campo por campo que falló.
   * Ejemplo: "price: debe ser mayor o igual a cero".
   * Los fallos de cada campo se agrupan en un array.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, List<String>> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));
    return this.buildValidationResponse(fieldErrors);
  }

  // Bad Request (400) para errores de validación al querer persistir o Error interno (500)
  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<Map<String, Object>> handleTransactionException(
      TransactionSystemException ex) {
    Throwable rootCause = ex.getRootCause();
    if (rootCause instanceof ConstraintViolationException constraintViolationException) {
      Map<String, List<String>> fieldErrors =
          this.extractConstraintViolations(constraintViolationException);
      return this.buildValidationResponse(fieldErrors);
    }
    log.error("Error interno de transacción:", rootCause != null ? rootCause : ex);
    return this.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
  }

  private Map<String, List<String>> extractConstraintViolations(ConstraintViolationException ex) {
    return ex.getConstraintViolations().stream()
        .collect(
            Collectors.groupingBy(
                violation -> violation.getPropertyPath().toString(),
                Collectors.mapping(ConstraintViolation::getMessage, Collectors.toList())));
  }

  // Error global (500)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
    log.error("Ocurrió un error interno inesperado:", ex);
    return this.buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MESSAGE);
  }

  private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
    return ResponseEntity.status(status)
        .body(
            Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message));
  }

  private ResponseEntity<Map<String, Object>> buildValidationResponse(
      Map<String, List<String>> fieldErrors) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "timestamp",
                LocalDateTime.now(),
                "status",
                HttpStatus.BAD_REQUEST.value(),
                "error",
                "Validation error",
                "message",
                "La validación de datos falló",
                "errors",
                fieldErrors));
  }
}
