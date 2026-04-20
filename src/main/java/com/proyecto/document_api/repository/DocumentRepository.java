package com.proyecto.document_api.repository;

import com.proyecto.document_api.dto.DocumentSummaryDTO;
import com.proyecto.document_api.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Interfaz que gestiona la comunicación directa con la base de datos PostgreSQL.
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    /**
     * Devuelve solo los datos básicos de todos los clientes usando el DTO optimizado.
     * Esto evita cargar el JSONB 'formulario' (que puede contener fotos pesadas).
     */
    @Query("SELECT new com.proyecto.document_api.dto.DocumentSummaryDTO(d.id, d.nombre, d.createdAt, d.updatedAt) FROM DocumentEntity d ORDER BY d.createdAt DESC")
    List<DocumentSummaryDTO> findAllSummaries();
}
