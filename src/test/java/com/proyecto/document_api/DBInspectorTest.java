package com.proyecto.document_api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.document_api.model.DocumentEntity;
import com.proyecto.document_api.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Map;

@SpringBootTest
class DBInspectorTest {

    @Autowired
    private DocumentRepository documentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void inspectDatabaseRecords() {
        System.out.println("====== INICIANDO INSPECCIÓN DE BASE DE DATOS ======");
        List<DocumentEntity> docs = documentRepository.findAll();
        // Ordenar por fecha de actualización descendente para ver los más nuevos
        docs.sort((a, b) -> {
            if (a.getUpdatedAt() == null && b.getUpdatedAt() == null) return 0;
            if (a.getUpdatedAt() == null) return 1;
            if (b.getUpdatedAt() == null) return -1;
            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
        });
        System.out.println("Total de documentos encontrados: " + docs.size());

        int count = 0;
        for (DocumentEntity doc : docs) {
            if (doc.getFormulario() != null) {
                try {
                    Map<String, Object> formMap = objectMapper.readValue(
                            doc.getFormulario(), new TypeReference<Map<String, Object>>() {});
                    
                    boolean foundInterestingKey = false;
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Object> entry : formMap.entrySet()) {
                        String key = entry.getKey();
                        String keyLower = key.toLowerCase();
                        if (keyLower.contains("elabora") || keyLower.contains("proyec") || keyLower.contains("fecha") || keyLower.contains("legalizacion")) {
                            sb.append("  '").append(key).append("' -> ").append(entry.getValue()).append("\n");
                            foundInterestingKey = true;
                        }
                    }
                    
                    if (foundInterestingKey) {
                        System.out.println("\nRegistro ID: " + doc.getId() + " | Nombre: " + doc.getNombre());
                        System.out.print(sb.toString());
                        count++;
                    }
                    
                    if (count >= 15) {
                        System.out.println("\nSe han inspeccionado 15 registros interesantes.");
                        break;
                    }
                } catch (Exception e) {
                    // Ignorar
                }
            }
        }
        System.out.println("====== FIN DE INSPECCIÓN ======");
    }
}
