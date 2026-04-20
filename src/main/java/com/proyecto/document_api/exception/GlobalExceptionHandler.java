package com.proyecto.document_api.exception;

import com.proyecto.document_api.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 * Intercepta cualquier error que ocurra en los controladores y lo convierte
 * en una respuesta JSON limpia usando el ErrorResponseDTO.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de validación del @Valid (ej: campo obligatorio vacío).
     * Devuelve un 400 Bad Request con los mensajes de validación.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String mensajes = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validación fallida")
                .message(mensajes)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura errores generales del servidor (ej: cliente no encontrado).
     * Devuelve un 500 Internal Server Error con el mensaje del error.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        // Si el mensaje indica que no se encontró el recurso, devolvemos 404
        HttpStatus status = ex.getMessage() != null && ex.getMessage().contains("No existe")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status(status.value())
                .error(status == HttpStatus.NOT_FOUND ? "Recurso no encontrado" : "Error del servidor")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }
}
