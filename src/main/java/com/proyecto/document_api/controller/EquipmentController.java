package com.proyecto.document_api.controller;

import com.proyecto.document_api.model.EquipmentEntity;
import com.proyecto.document_api.repository.EquipmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/equipment")
@Tag(name = "Gestión de Equipos", description = "Endpoints para administrar Módulos, Inversores y Baterías")
@CrossOrigin(origins = "*")
public class EquipmentController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Operation(summary = "Listar equipos por tipo")
    @GetMapping("/{tipo}")
    public List<Map<String, Object>> getByType(@PathVariable String tipo) {
        return equipmentRepository.findByTipo(tipo).stream().map(entity -> {
            Map<String, Object> flatMap = new java.util.HashMap<>();
            flatMap.put("id", entity.getId());
            flatMap.put("tipo", entity.getTipo());
            flatMap.put("nombre", entity.getNombre());
            flatMap.put("createdAt", entity.getCreatedAt());
            
            // Metemos todo lo que hay dentro de 'datos' al nivel principal
            if (entity.getDatos() != null) {
                flatMap.putAll(entity.getDatos());
            }
            return flatMap;
        }).collect(Collectors.toList());
    }

    @Operation(summary = "Crear nuevo equipo")
    @PostMapping("/{tipo}")
    public ResponseEntity<EquipmentEntity> create(@PathVariable String tipo, @RequestBody Map<String, Object> payload) {
        System.out.println("[Backend] Recibida petición POST para tipo: " + tipo);
        System.out.println("[Backend] Payload: " + payload);
        
        EquipmentEntity equipment = new EquipmentEntity();
        equipment.setTipo(tipo);
        
        // El ID lo sacamos del payload o generamos uno
        String id = (String) payload.getOrDefault("id", "");
        if (id.isEmpty()) {
            String prefix = tipo.substring(0, 3).toLowerCase() + "_";
            id = prefix + UUID.randomUUID().toString().substring(0, 8);
        }
        equipment.setId(id);
        
        // El nombre comercial
        equipment.setNombre((String) payload.getOrDefault("nombre", payload.getOrDefault("marcaModelo", payload.getOrDefault("label", "Sin nombre"))));
        
        // El resto va a la columna JSONB 'datos'
        payload.remove("id");
        payload.remove("tipo");
        payload.remove("nombre");
        equipment.setDatos(payload);
        
        return ResponseEntity.ok(equipmentRepository.save(equipment));
    }

    @Operation(summary = "Actualizar equipo existente")
    @PutMapping("/{tipo}/{id}")
    public ResponseEntity<EquipmentEntity> update(@PathVariable String tipo, @PathVariable String id, @RequestBody Map<String, Object> payload) {
        return equipmentRepository.findById(id)
                .map(existing -> {
                    existing.setNombre((String) payload.getOrDefault("nombre", payload.getOrDefault("marcaModelo", payload.getOrDefault("label", existing.getNombre()))));
                    
                    payload.remove("id");
                    payload.remove("tipo");
                    payload.remove("nombre");
                    existing.setDatos(payload);
                    
                    return ResponseEntity.ok(equipmentRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Eliminar equipo")
    @DeleteMapping("/{tipo}/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tipo, @PathVariable String id) {
        if (equipmentRepository.existsById(id)) {
            equipmentRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
