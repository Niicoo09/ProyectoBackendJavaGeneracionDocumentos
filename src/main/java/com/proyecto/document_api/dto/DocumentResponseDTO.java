package com.proyecto.document_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * DTO para devolver los datos completos de un cliente cuando se abre su formulario de edición.
 * Envuelve el JSON del formulario junto con los metadatos del registro (id, nombre, fechas).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {
    private UUID id;
    private String nombre;
    private Map<String, Object> data;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
