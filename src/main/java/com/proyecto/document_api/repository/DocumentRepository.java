package com.proyecto.document_api.repository;

import com.proyecto.document_api.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Interfaz que gestiona la comunicación directa con la base de datos PostgreSQL.
 * Permite buscar la información de los clientes de forma automática.
 * 
 * @author Nicolas Navarro Contreras
 */
@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
}
