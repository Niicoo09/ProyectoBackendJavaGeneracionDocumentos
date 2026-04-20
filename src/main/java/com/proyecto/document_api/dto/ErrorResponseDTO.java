package com.proyecto.document_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO estándar para todas las respuestas de error de la API.
 * Garantiza que el Frontend siempre reciba el mismo formato de error,
 * independientemente de dónde falle el servidor.
 *
 * Ejemplo de respuesta JSON:
 * {
 *   "status": 400,
 *   "error": "Validación fallida",
 *   "message": "El nombre del cliente no puede estar vacío.",
 *   "path": "/api/v1/documents",
 *   "timestamp": "2026-04-20T09:00:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
