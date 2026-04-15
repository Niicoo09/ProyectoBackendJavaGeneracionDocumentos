package com.proyecto.document_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    
    // Este código UUID es la clave única que identifica a cada cliente en el dashboard
    @Id
    private UUID id; 

    // Aquí se guarda el nombre completo del cliente
    @Column(name = "nombre")
    private String nombre;

    // Aquí se guarda TODA la información técnica en formato texto (NIF, importes, etc.)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "formulario", columnDefinition = "jsonb")
    private String formulario;

    // Fechas de creación y actualización automática de la base de datos
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
