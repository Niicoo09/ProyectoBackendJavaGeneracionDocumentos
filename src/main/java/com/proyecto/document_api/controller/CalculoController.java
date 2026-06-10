package com.proyecto.document_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de utilidad para realizar cálculos de presupuestos técnicos
 * requeridos por las trabajadoras y el frontend.
 */
@RestController
@RequestMapping("/api/v1/calculos")
@Tag(name = "Cálculos Auxiliares", description = "Endpoints para cálculos automatizados de presupuestos y tasas")
public class CalculoController {

    @Operation(
        summary = "Calcular Desglose PEM", 
        description = "Calcula el desglose del P.E.M., P.E.C., IVA, Gastos Generales y Beneficio Industrial a partir del Presupuesto Total con IVA."
    )
    @GetMapping("/pem")
    public ResponseEntity<Map<String, Object>> calcularPem(@RequestParam Double presupuestoTotal) {
        if (presupuestoTotal == null || presupuestoTotal <= 0) {
            return ResponseEntity.badRequest().build();
        }

        double totalVal = presupuestoTotal;
        
        // Cálculos con precisión completa sin redondeo intermedio
        double pecVal = totalVal / 1.21;
        double pemVal = pecVal / 1.19;
        double gastosGeneralesVal = pemVal * 0.13;
        double beneficioIndustrialVal = pemVal * 0.06;
        double ivaVal = pecVal * 0.21;

        // Redondeamos a 2 decimales para la respuesta
        BigDecimal total = BigDecimal.valueOf(totalVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pem = BigDecimal.valueOf(pemVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal gastosGenerales = BigDecimal.valueOf(gastosGeneralesVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal beneficioIndustrial = BigDecimal.valueOf(beneficioIndustrialVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pec = BigDecimal.valueOf(pecVal).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = BigDecimal.valueOf(ivaVal).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("presupuestoTotalConIva", total.doubleValue());
        result.put("pem", pem.doubleValue());
        result.put("gastosGenerales", gastosGenerales.doubleValue());
        result.put("beneficioIndustrial", beneficioIndustrial.doubleValue());
        result.put("pec", pec.doubleValue());
        result.put("iva", iva.doubleValue());

        return ResponseEntity.ok(result);
    }
}
