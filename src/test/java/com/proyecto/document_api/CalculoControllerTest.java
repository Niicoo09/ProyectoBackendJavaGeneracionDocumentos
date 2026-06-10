package com.proyecto.document_api;

import com.proyecto.document_api.controller.CalculoController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class CalculoControllerTest {

    private final CalculoController calculoController = new CalculoController();

    @Test
    void testCalcularPemValoresCorrectos() {
        ResponseEntity<Map<String, Object>> response = calculoController.calcularPem(10200.00);
        assertTrue(response.getStatusCode().is2xxSuccessful());
        
        Map<String, Object> result = response.getBody();
        assertNotNull(result);
        assertEquals(10200.00, result.get("presupuestoTotalConIva"));
        assertEquals(7083.83, result.get("pem"));
        assertEquals(920.90, result.get("gastosGenerales"));
        assertEquals(425.03, result.get("beneficioIndustrial"));
        assertEquals(8429.75, result.get("pec"));
        assertEquals(1770.25, result.get("iva"));
    }

    @Test
    void testCalcularPemValoresInvalidos() {
        ResponseEntity<Map<String, Object>> responseNull = calculoController.calcularPem(null);
        assertTrue(responseNull.getStatusCode().is4xxClientError());

        ResponseEntity<Map<String, Object>> responseNegative = calculoController.calcularPem(-100.00);
        assertTrue(responseNegative.getStatusCode().is4xxClientError());
    }
}
