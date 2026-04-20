package com.proyecto.document_api.controller;

import com.proyecto.document_api.dto.DocumentCreateDTO;
import com.proyecto.document_api.dto.DocumentResponseDTO;
import com.proyecto.document_api.dto.DocumentSummaryDTO;
import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import com.proyecto.document_api.repository.DocumentRepository;

/**
 * Controlador dedicado exclusivamente a la gestión de datos de los clientes.
 * Separa la persistencia (CRUD) de la generación de documentos PDF.
 */
@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Gestión de Datos", description = "Endpoints para crear, editar y listar la información técnica de los clientes")
public class DocumentDataController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    /**
     * Obtiene la lista completa de clientes para el Dashboard (OPTIMIZADO).
     */
    @Operation(summary = "Listar Clientes", description = "Devuelve solo los datos básicos para el panel principal.")
    @GetMapping
    public List<DocumentSummaryDTO> getAll() {
        return documentRepository.findAllSummaries();
    }

    /**
     * Obtiene los datos completos de un cliente envueltos en el DocumentResponseDTO.
     * Incluye metadatos (id, nombre, fechas) + el JSON del formulario.
     */
    @Operation(summary = "Obtener Datos de Formulario", description = "Recupera el objeto JSON completo del cliente para edición, junto con sus metadatos.")
    @GetMapping("/data/{id}")
    public ResponseEntity<DocumentResponseDTO> getFormData(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocumentResponse(id));
    }

    /**
     * Crea un nuevo cliente con un UUID generado.
     * @Valid activa las validaciones del DocumentCreateDTO (nombre obligatorio, etc.)
     */
    @Operation(summary = "Crear Nuevo Cliente", description = "Registra un nuevo expediente en la base de datos.")
    @PostMapping
    public ResponseEntity<DocumentSummaryDTO> createDocument(@Valid @RequestBody DocumentCreateDTO dto) {
        UUID newId = UUID.randomUUID();
        DocumentEntity saved = documentService.saveDocumentFromDTO(newId, dto);
        DocumentSummaryDTO summary = new DocumentSummaryDTO(
                saved.getId(), saved.getNombre(), saved.getCreatedAt(), saved.getUpdatedAt());
        return new ResponseEntity<>(summary, HttpStatus.CREATED);
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @Valid activa las validaciones del DocumentCreateDTO.
     */
    @Operation(summary = "Actualizar Cliente", description = "Modifica los datos técnicos de un expediente existente.")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentSummaryDTO> updateDocument(
            @PathVariable UUID id,
            @Valid @RequestBody DocumentCreateDTO dto) {
        DocumentEntity saved = documentService.saveDocumentFromDTO(id, dto);
        DocumentSummaryDTO summary = new DocumentSummaryDTO(
                saved.getId(), saved.getNombre(), saved.getCreatedAt(), saved.getUpdatedAt());
        return ResponseEntity.ok(summary);
    }
}
