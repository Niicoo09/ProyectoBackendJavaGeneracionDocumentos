package com.proyecto.document_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa la tabla de generación de documentos en la base de datos de
 * Coolify.
 * Mapea los campos de los clientes para que Java pueda entenderlos.
 * 
 * @author Nicolas Navarro Contreras
 */
@Entity
@Table(name = "generacion_docs", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentEntity {
    @Id
    private UUID id; // Cambiado de Long a UUID

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "formulario")
    private String formulario;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
