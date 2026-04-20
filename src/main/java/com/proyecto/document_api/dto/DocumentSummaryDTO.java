package com.proyecto.document_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO optimizado para la lista del Dashboard.
 * No incluye el campo 'formulario' (JSON/Base64) para aligerar la carga.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummaryDTO {
    private UUID id;
    private String nombre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
