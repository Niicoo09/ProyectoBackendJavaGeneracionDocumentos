package com.proyecto.document_api.controller;

import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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
     * Obtiene la lista completa de clientes para el Dashboard.
     */
    @Operation(summary = "Listar Clientes", description = "Devuelve todos los registros para mostrar en el panel principal.")
    @GetMapping
    public List<DocumentEntity> getAll() {
        return documentRepository.findAll();
    }

    /**
     * Obtiene el JSON crudo de un cliente para rellenar el MasterForm en Vue.
     */
    @Operation(summary = "Obtener Datos de Formulario", description = "Recupera el objeto JSON completo del cliente para edición.")
    @GetMapping("/data/{id}")
    public ResponseEntity<Map<String, Object>> getFormData(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.getDocumentForm(id));
    }

    /**
     * Crea un nuevo cliente con un UUID generado.
     */
    @Operation(summary = "Crear Nuevo Cliente", description = "Registra un nuevo expediente en la base de datos.")
    @PostMapping
    public ResponseEntity<DocumentEntity> createDocument(@RequestBody Map<String, Object> formData) {
        // Si no viene ID en el Map, generamos uno nuevo
        UUID newId = UUID.randomUUID();
        DocumentEntity saved = documentService.saveDocument(newId, formData);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * Actualiza los datos de un cliente existente.
     */
    @Operation(summary = "Actualizar Cliente", description = "Modifica los datos técnicos de un expediente existente.")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentEntity> updateDocument(
            @PathVariable UUID id, 
            @RequestBody Map<String, Object> formData) {
        return ResponseEntity.ok(documentService.saveDocument(id, formData));
    }
}
