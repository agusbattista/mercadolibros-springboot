package io.github.agusbattista.mercadolibros_springboot.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Recurso no encontrado (404)
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    return this.buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  // Conflico / Duplicado (409)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(IllegalArgumentException ex) {
    return this.buildResponse(HttpStatus.CONFLICT, ex.getMessage());
  }

  /*
    Error de validación - @Valid (400)
    Extrae campo por campo que falló. (Ejemplo: "price": debe ser mayor a cero")
    Los fallos de cada campo se agrupan en un array
  */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
    errorResponse.put("error", "Validation Error");

    Map<String, List<String>> fieldErrors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error ->
                fieldErrors
                    .computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage()));

    errorResponse.put("message", "La validación de datos falló");
    errorResponse.put("errors", fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("timestamp", LocalDateTime.now());
    error.put("status", status.value());
    error.put("error", status.getReasonPhrase());
    error.put("message", message);
    return ResponseEntity.status(status).body(error);
  }

  //Bad Request (400) - Cuerpo de la petición vacío o inválido
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
    return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es vacío o inválido");
  }

  //Bad Request (400) para errores de validación al querer persistir o Error interno (500)
  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<Map<String, Object>> handleTransactionException(TransactionSystemException ex) {
    Throwable rootCause = ex.getRootCause();

    if (rootCause instanceof ConstraintViolationException) {
      ConstraintViolationException constraintViolationException = (ConstraintViolationException) rootCause;

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("timestamp", LocalDateTime.now());
      errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
      errorResponse.put("error", "Validation Error");

      Map<String, List<String>> fieldErrors = new HashMap<>();
      for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
        String field = violation.getPropertyPath().toString();
        String message = violation.getMessage();
        fieldErrors
            .computeIfAbsent(field, key -> new ArrayList<>())
            .add(message);
      }

      errorResponse.put("message", "Error al validar los datos");
      errorResponse.put("errors", fieldErrors);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno de transacción");
  }
}
