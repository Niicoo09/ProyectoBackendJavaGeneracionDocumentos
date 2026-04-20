package com.proyecto.document_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * DTO de entrada para crear o actualizar un cliente.
 * Las anotaciones de @NotBlank y @NotNull actúan como validadores automáticos:
 * si el Frontend no envía alguno de estos campos, el servidor responde con
 * un error 400 claro, en lugar de guardar datos incompletos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentCreateDTO {

    /**
     * El nombre del cliente es obligatorio para identificar el expediente.
     * Viene del campo 'apellidosNombre' del formulario.
     */
    @NotBlank(message = "El nombre del cliente no puede estar vacío.")
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres.")
    private String nombre;

    /**
     * El JSON del formulario con todos los datos técnicos del cliente.
     * No puede ser nulo, aunque puede venir con campos vacíos.
     */
    @NotNull(message = "Los datos del formulario son obligatorios.")
    private Map<String, Object> formulario;
}
