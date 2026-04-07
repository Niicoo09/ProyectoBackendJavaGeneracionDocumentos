package com.proyecto.document_api.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad técnica para procesar el JSON de la columna 'formulario'.
 * Convierte el texto plano en un objeto Map que Java y Thymeleaf pueden leer.
 * 
 * @author Nicolas Navarro Contreras
 */
@Component
public class JsonUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte un String JSON en un Mapa de datos.
     * Si falla, devuelve un mapa vacío para evitar que la aplicación se detenga.
     */
    public Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("Error al parsear el JSON del formulario: " + e.getMessage());
            return new HashMap<>();
        }
    }
}
