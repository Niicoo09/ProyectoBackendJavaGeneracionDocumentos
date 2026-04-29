package com.proyecto.document_api.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que replica la lógica de `defaultData` y `fieldMapping` del archivo
 * `documents.js` del proyecto Vue. Se encarga de:
 * 1. Aplicar valores por defecto (datos del técnico, empresa, etc.) que NO
 * vienen en el JSON de BD.
 * 2. Mapear campos de la BD al nombre que espera la plantilla Thymeleaf.
 *
 * Se llama ANTES de pasar los datos al motor de plantillas.
 */
@Service
public class DocumentConfigService {

    // =========================================================================
    // VALORES FIJOS DEL TÉCNICO (Eduardo Rivera Cabezas)
    // =========================================================================
    private static final String TECNICO_NOMBRE = "Eduardo Rivera Cabezas";
    private static final String TECNICO_NIF = "28818007L";
    private static final String TECNICO_NUMERO_COLEGIADO = "4654";
    private static final String TECNICO_COLEGIO = "Colegio Oficial de Ingenieros Industriales de Andalucía Occidental";
    private static final String TECNICO_COLEGIO_CORTO = "4654 COIIAOC";
    private static final String TECNICO_DOMICILIO = "Calle Ebro";
    private static final String TECNICO_NUMERO = "35";
    private static final String TECNICO_LOCALIDAD = "Sevilla";
    private static final String TECNICO_CP = "41.012";
    private static final String TECNICO_TELEFONO = "629 118 196";
    private static final String EMPRESA_NUMERO_EMPRESA = "41045500";

    /**
     * Enriquece el `formData` con defaultData y fieldMapping para una plantilla
     * dada.
     *
     * @param templateName Nombre de la plantilla (sin .html)
     * @param formData     Mapa de datos tal como vienen de la base de datos
     * @return Nuevo mapa enriquecido listo para Thymeleaf
     */
    public Map<String, Object> enrich(String templateName, Map<String, Object> formData) {
        Map<String, Object> enriched = new HashMap<>(formData);

        // --- LIMPIEZA PREVENTIVA GLOBAL DE IDENTIFICADORES ---
        // Limpiamos los campos más comunes de identificación para que estén listos en
        // cualquier plantilla
        String[] idFields = { "nifCif", "nif", "dni", "cif", "dniRepresentante", "nifRepresentante" };
        for (String field : idFields) {
            if (enriched.containsKey(field) && enriched.get(field) != null) {
                enriched.put(field, cleanDni(enriched.get(field).toString()));
            }
        }

        // Extraer solo el nombre base de la plantilla (eliminando carpetas si existen)
        String baseTemplateName = templateName;
        if (templateName.contains("/")) {
            baseTemplateName = templateName.substring(templateName.lastIndexOf("/") + 1);
        }

        switch (baseTemplateName) {
            // --- MEMORIAS TÉCNICAS (MTD) ---
            case "mtd-instalacion-autoconsumo-monofasica-con-bateria":
            case "mtd-instalacion-aislada-con-bateria":
            case "mtd-instalacion-autoconsumo-trifasica-con-bateria":
            case "mtd-instalacion-autoconsumo-sin-bateria":
            case "mtd-instalacion-puntos-recarga":
            case "mtd-monofasica-con-bateria":
            case "mtd-aislada-con-bateria":
            case "mtd-trifasica-con-bateria":
            case "mtd-sin-bateria":
            case "mtd-punto-recarga":
            case "MemoriaTecnica":
            case "MemoriaTecnicaSinBateria":
            case "MemoriaTecnicaTrifasica":
            case "MemoriaTecnicaAislada":
            case "MemoriaTecnicaPuntoRecarga":
                applyMemoriaTecnicaCommon(enriched, formData, baseTemplateName);
                break;

            // --- CERTIFICADOS DE SOLIDEZ ---
            case "certificado-coplanar-teja":
            case "certificado-aporticada-teja":
            case "aporticado-teja":
            case "certificado-cubierta-plan-aaporticada":
            case "certificado-chapas-grecadas-aporticada":
            case "chapas-grecadas":
            case "certificado-chapas-grecadas-coplanaria":
            case "chapas-grecadas-coplanaria":
            case "certificado-paramento-vertical":
            case "paramento-vertical":
            case "certificado-pergola-aporticada":
            case "pergola-aporticada":
            case "CertificadoCoplanarTeja":
            case "CertificadoAporticadaTeja":
            case "CertificadoCubiertaPlanaAporticada":
            case "CertificadoChapasGrecadasAporticadas":
            case "CertificadoChapasGrecadasCoplanaria":
            case "CertificadoParamentoVertical":
            case "CertificadoPergolaAporticada":
                applyCertificadoSolidez(enriched, formData);
                break;

            // --- ACEPTACIONES ---
            case "aceptacion-subvencion":
            case "AceptacionSubvencion":
                applyAceptacionSubvencion(enriched, formData);
                break;

            // --- JUSTIFICACIONES ---
            case "justificacion-pago-subvencion-l3":
            case "JustificacionPagoSubvencionL3":
                applyJustificacionPagoSubvencionL3(enriched, formData);
                break;
            case "justificacion-pago-subvencion-l4":
            case "JustificacionPagoSubvencionL4":
            case "L3PagoAnticipado50":
            case "L3PagoRestante50":
            case "L4PagoAnticipado100":
                applyJustificacionPagoSubvencionL3(enriched, formData);
                break;
            case "memoria-economica":
            case "MemoriaEconomica":
                applyMemoriaEconomica(enriched, formData);
                break;
            case "memoria-fv-aer":
            case "MemoriaFvAer":
                applyMemoriaFvAer(enriched, formData);
                break;
            case "obra-massol":
            case "ObraMassol":
                applyObraMassol(enriched, formData);
                break;
            case "declaracion-compromiso-corriente":
            case "DeclaracionCompromisoCorriente":
                applyDeclaracionCompromisoCorriente(enriched, formData);
                break;
            case "certificado-pedidos":
            case "certificado-pedidos-contratos":
            case "CertificadoPedidos":
                applyCertificadoPedidosContratos(enriched, formData);
                break;

            // --- OTROS DOCUMENTOS ---
            case "autorizacion-representacion":
                applyAutorizacionRepresentacion(enriched, formData);
                break;
            case "declaracion-habilitacion-profesional":
            case "habilitacion-profesional":
            case "DeclaracionHabilitacionProfesional":
                applyDeclaracionHabilitacion(enriched, formData);
                break;
            case "planos":
            case "planos-situacion-emplazamiento-cubierta":
            case "PlanosSituacionEmplazamientoCubierta":
                applyPlanosSituacionEmplazamiento(enriched, formData);
                break;
            case "documento-80-paginas":
            case "estudio-seguridad":
            case "EstudioSeguridadSalud":
                applyEstudioSeguridadSalud(enriched, formData);
                break;
            case "no-generacion-rcds":
            case "no-generacion-residuos":
            case "declaracion-no-generacion-rcds":
                applyDeclaracionNoGeneracionRcds(enriched, formData);
                break;
            case "AnexoIii":
            case "AnexoIII":
            case "anexo-iii":
            case "autorizacion-comunicacion":
                applyAutorizacionComunicacion(enriched, formData);
                break;
            case "adecuacion":
            case "certificado-adecuacion":
            case "CertificadoAdecuacion":
                applyCertificadoAdecuacion(enriched, formData);
                break;
            case "cie":
            case "z-certificado-br":
                applyCie(enriched, formData);
                break;
            case "z-certificado-doacfv":
            case "doacfv":
                applyCertificadoDireccionObra(enriched, formData);
                break;
            case "declaracion-responsable-do":
            case "certificado-direccion-obra":
            case "z-declaracion-direccion-obra":
                applyDeclaracionDireccionObra(enriched, formData);
                break;
            case "dr-tecnico-competente":
            case "z-declaracion-tecnico-competente":
                applyDeclaracionTecnicoCompetente(enriched, formData);
                break;
            case "documento-ultima-pagina":
                applyDocumentoUltimaPagina(enriched, formData);
                break;
            case "CartelL3":
            case "cartel-l3":
                applyCartelL3(enriched, formData);
                break;
            case "CartelL4":
            case "cartel-l4":
                applyCartelL4(enriched, formData);
                break;
            case "declaracion-propietario":
                applyDeclaracionPropietario(enriched, formData);
                break;
            case "autorizacion-facturacion":
                applyAutorizacionFacturacion(enriched, formData);
                break;

            default:
                applyCommonFieldMapping(enriched, formData);
                break;
        }

        // --- SANITIZACIÓN FINAL ---
        // Limpiamos caracteres especiales corruptos en todo el mapa antes de devolverlo
        return sanitizeMap(enriched);
    }

    // =========================================================================
    // ACEPTACIÓN DE SUBVENCIÓN
    // =========================================================================
    private void applyAceptacionSubvencion(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "razonSocial", "apellidosNombre");

        // DNI limpio
        String dniRaw = getString(form, "nifCif");
        enriched.put("dni", cleanDni(dniRaw));

        applyMapping(enriched, form, "expedienteEco", "expedienteEco");
        applyMapping(enriched, form, "codigoEni", "codigoEni");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "edificioVivienda", "edificioVivienda");
        applyMapping(enriched, form, "importeSubvencion", "importeSubvencionResultante");
        applyMapping(enriched, form, "nombreFirma", "apellidosNombre");

        applyMapping(enriched, form, "dia", "diaAceptacion");
        applyMapping(enriched, form, "mes", "mesAceptacion");
        applyMapping(enriched, form, "anio", "anioAceptacion");

        putIfAbsent(enriched, "ciudad", form.get("localidadEmplazamiento"));
    }

    private void applyDeclaracionAusenciaConflicto(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "nombreFirma", "apellidosNombre");
        applyMapping(enriched, form, "dia", "diaAceptacion");
        applyMapping(enriched, form, "mes", "mesAceptacion");
        applyMapping(enriched, form, "anio", "anioAceptacion");
        applyMapping(enriched, form, "ciudad", "provinciaEmplazamiento");
    }

    private void applyDeclaracionCesionTratamiento(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "nombreApellidos", "apellidosNombre");

        // NIF limpio
        enriched.put("nif", cleanDni(getString(form, "nifCif")));

        enriched.put("direccion", buildDireccionCompleta(form));

        // Datos del Representante (solo si existe)
        String rep = getString(form, "representante");
        if (rep != null && !rep.trim().isEmpty()) {
            applyMapping(enriched, form, "nombreRepresentante", "representante");
            enriched.put("dniRepresentante", cleanDni(getString(form, "dniRepresentante")));
            applyMapping(enriched, form, "calidad", "representanteCargo");
            putIfAbsent(enriched, "calidad", "Representante de la sociedad");
        } else {
            enriched.put("nombreRepresentante", "");
            enriched.put("dniRepresentante", "");
            enriched.put("calidad", "");
        }

        applyMapping(enriched, form, "nombreEntidadRepresentada", "razonSocial");
        enriched.put("nifEntidadRepresentada", cleanDni(getString(form, "nifCif")));

        // Pie de firma
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");
        applyMapping(enriched, form, "ciudad", "localidadEmplazamiento");
        applyMapping(enriched, form, "nombreFirma", "apellidosNombre");
    }

    private void applyDeclaracionCompromisoDerechos(Map<String, Object> enriched, Map<String, Object> form) {
        // Mismos campos que Cesión de Tratamiento
        applyDeclaracionCesionTratamiento(enriched, form);
    }

    private void applyDeclaracionCompromisoTransversales(Map<String, Object> enriched, Map<String, Object> form) {
        // Mismos campos que Cesión de Tratamiento
        applyDeclaracionCesionTratamiento(enriched, form);
    }

    private void applyCartelL3(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "costeActuacion", "e2_potenciaPicoGenerador"); // Placeholder
        applyMapping(enriched, form, "cuantiaSubvencion", "importeSubvencion");

        String ubicacion = buildDireccionCompleta(form) + ", " + getString(form, "localidadEmplazamiento");
        enriched.put("entidadUbicacion", ubicacion);
    }

    private void applyCartelL4(Map<String, Object> enriched, Map<String, Object> form) {
        // Reutilizamos la lógica del L3 ya que los campos son idénticos
        applyCartelL3(enriched, form);
    }

    private void applyDeclaracionPropietario(Map<String, Object> enriched, Map<String, Object> form) {
        applyCommonFieldMapping(enriched, form);
        applyMapping(enriched, form, "apellidosNombre", "apellidosNombre");
        enriched.put("nifCif", cleanDni(getString(form, "nifCif")));
        
        // Dirección detallada
        enriched.put("direccionCompleta", buildDireccionCompleta(form));
        applyMapping(enriched, form, "emplazamientoCalle", "emplazamientoCalle");
        applyMapping(enriched, form, "numero", "numero");
        applyMapping(enriched, form, "bloque", "bloque");
        applyMapping(enriched, form, "escalera", "escalera");
        applyMapping(enriched, form, "planta", "planta");
        applyMapping(enriched, form, "puerta", "puerta");
        
        applyEmailMapping(enriched, form, "correoElectronico");
        applyEmailMapping(enriched, form, "correoElectronicoEmplazamiento"); // Por compatibilidad con PAC
        applyMapping(enriched, form, "telefono", "telefono");
        applyMapping(enriched, form, "referenciaCatastral", "referenciaCatastral");
        applyMapping(enriched, form, "tipoInstalacion", "tipoInstalacion");
        applyMapping(enriched, form, "potencia_instalacion", "e2_potenciaNominalInversores");

        // Fecha
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");
    }

    private void applyAutorizacionFacturacion(Map<String, Object> enriched, Map<String, Object> form) {
        applyDeclaracionPropietario(enriched, form); // Reutiliza la mayoría de los campos
    }

    private void applyDeclaracionCompromisoCorriente(Map<String, Object> enriched, Map<String, Object> form) {
        // Beneficiario
        applyMapping(enriched, form, "apellidosNombreBeneficiario", "apellidosNombre");
        enriched.put("dniBeneficiario", cleanDni(getString(form, "nifCif")));
        enriched.put("domicilioBeneficiario", buildDireccionCompleta(form));

        // Empresa / Razón Social
        applyMapping(enriched, form, "razonSocial", "razonSocial");
        enriched.put("nifEmpresa", cleanDni(getString(form, "nifCif")));
        enriched.put("domicilioFiscal", buildDireccionCompleta(form));

        // Representante Legal
        applyMapping(enriched, form, "apellidosNombreRepresentanteLegal", "representante");
        enriched.put("dniRepresentanteLegal", cleanDni(getString(form, "dniRepresentante")));

        // Agente Gestor (Valores fijos de SOLAY según Vue)
        putIfAbsent(enriched, "apellidosNombreAgenteGestor", "SOLAY INGENIEROS S.L.");
        putIfAbsent(enriched, "dniNieAgenteGestor", "B09848912");
        putIfAbsent(enriched, "apellidosNombrePersonaRepresentante", "Miguel Angel Rivas Zapata");
        applyMapping(enriched, form, "dniNiePersonaRepresentante", "dniRepresentante");

        // Fecha y Lugar
        applyMapping(enriched, form, "lugar", "localidadEmplazamiento");
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");
    }

    private void applyJustificacionPagoSubvencionL3(Map<String, Object> enriched, Map<String, Object> form) {
        // Datos Personales
        applyMapping(enriched, form, "expediente", "expedienteEco");
        applyMapping(enriched, form, "apellidosNombre", "apellidosNombre");

        // DNI limpio (sin puntos)
        String dniRaw = getString(form, "nifCif");
        enriched.put("dni", cleanDni(dniRaw));

        // Teléfono con fallback
        applyMappingWithFallback(enriched, form, "telefono", "telefonos", "telefonos1", "telefono");

        // Correo con fallback
        applyEmailMapping(enriched, form, "correoElectronico");

        // Representante
        applyMapping(enriched, form, "apellidosNombreRepresentante", "representante");

        // DNI Representante limpio (sin puntos)
        String dniRepRaw = getString(form, "dniRepresentante");
        enriched.put("dniRepresentante", cleanDni(dniRepRaw));

        applyMapping(enriched, form, "actuaCalidad", "representanteCargo");

        // Fallbacks para representante
        applyMappingWithFallback(enriched, form, "telefonoRepresentante", "telefonoRepresentante", "telefonos",
                "telefono");
        applyMappingWithFallback(enriched, form, "correoElectronicoRepresentante", "correoElectronicoRepresentante",
                "emailRepresentante", "email");

        // Pie de firma
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");
        applyMapping(enriched, form, "personaFirma", "apellidosNombre");

        // --- VALORES POR DEFECTO PARA EVITAR ERRORES SPEL ---
        putIfAbsent(enriched, "expediente", "");
        putIfAbsent(enriched, "apellidosNombre", "");
        putIfAbsent(enriched, "dni", "");
        putIfAbsent(enriched, "telefono", "");
        putIfAbsent(enriched, "correoElectronico", "");
        putIfAbsent(enriched, "apellidosNombreRepresentante", "");
        putIfAbsent(enriched, "dniRepresentante", "");
        putIfAbsent(enriched, "telefonoRepresentante", "");
        putIfAbsent(enriched, "correoElectronicoRepresentante", "");
        putIfAbsent(enriched, "provincia", "");
        putIfAbsent(enriched, "dia", "");
        putIfAbsent(enriched, "mes", "");
        putIfAbsent(enriched, "anio", "");
        putIfAbsent(enriched, "personaFirma", "");
        putIfAbsent(enriched, "actuaCalidad", "");
    }

    private void applyJustificacionPagoSubvencionL4(Map<String, Object> enriched, Map<String, Object> form) {
        // Reutilizamos la lógica del L3 (mismos campos, solo cambian coordenadas en
        // HTML)
        applyJustificacionPagoSubvencionL3(enriched, form);
    }

    private void applyMemoriaEconomica(Map<String, Object> enriched, Map<String, Object> form) {
        // Identificación
        applyMapping(enriched, form, "expediente", "expedienteEco");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));

        // Contexto
        String linea = getString(form, "lineaAyuda");
        enriched.put("l3l4", (linea != null && linea.contains("4")) ? "Línea 4" : "Línea 3");
        enriched.put("edificioVivienda", "VIVIENDA"); // Valor por defecto común

        // Importes Generales
        applyMapping(enriched, form, "totalCantidadJustificada", "totalContrato");
        applyMapping(enriched, form, "presupuestoInicial", "totalContrato");
        applyMapping(enriched, form, "inversionRealizada", "totalContrato");
        enriched.put("desviacion", "0,00");

        // Tabla de Gastos (5 Filas)
        for (int i = 1; i <= 5; i++) {
            applyMapping(enriched, form, "numeroFactura" + i, "numeroFactura" + i);
            applyMapping(enriched, form, "fechaFactura" + i, "fechaFactura" + i);
            applyMapping(enriched, form, "cf" + i, "cf" + i);
            applyMapping(enriched, form, "acreedor" + i, "acreedor" + i);
            applyMapping(enriched, form, "concepto" + i, "concepto" + i);
            applyMapping(enriched, form, "fechaPago" + i, "fechaPago" + i);
            applyMapping(enriched, form, "importe" + i, "importe" + i);
        }

        // Fecha Firma
        applyMapping(enriched, form, "diaFirmaJustificacion", "dia");
        applyMapping(enriched, form, "mesFirmaJustificacion", "mes");
        applyMapping(enriched, form, "anioFirmaJustificacion", "anio");
        applyMapping(enriched, form, "nombreFirma", "apellidosNombre");
    }

    private void applyMemoriaFvAer(Map<String, Object> enriched, Map<String, Object> form) {
        // Identificación
        applyMapping(enriched, form, "expedienteEco", "expedienteEco");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));
        enriched.put("domicilio", buildDireccionCompleta(form));

        String linea = getString(form, "lineaAyuda");
        enriched.put("l3l4", (linea != null && linea.contains("4")) ? "Línea 4" : "Línea 3");
        enriched.put("tipoEdificio", "VIVIENDA");

        // Aerotermia (Detección dinámica)
        String potBomba = getString(form, "potenciaBombaCalle");
        boolean hasAerotermia = potBomba != null && !potBomba.isEmpty();
        enriched.put("aerotermia", hasAerotermia);
        applyMapping(enriched, form, "potenciaBomba", "potenciaBombaCalle");
        applyMapping(enriched, form, "depositoLitros", "volumenAcumulacion");
        applyMapping(enriched, form, "caracteristicasAerotermia", "caracteristicasAerotermia");

        // Fechas
        applyMapping(enriched, form, "fechaConclusionDia", "dia");
        applyMapping(enriched, form, "fechaConclusionMes", "mes");
        applyMapping(enriched, form, "fechaConclusionAnio", "anio");
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");

        // Técnico (Miguel Ángel Rivas Zapata) - Ya viene por defecto en plantilla pero
        // se puede mapear

        // Soporte
        applyMapping(enriched, form, "tipoSoporte", "e10_tipoSoporteOtejado");

        // Importes
        applyMapping(enriched, form, "importeActuacionesSinIva", "importeSubvencion"); // Placeholder
        String importeConIva = String.format("%.2f", parseDouble(getString(form, "importeSubvencion")) * 1.21);
        enriched.put("importeActuacionesConIva", importeConIva);
        enriched.put("coincidencia", "SÍ");

        // Acreditación (Reducción consumo energía primaria)
        applyMapping(enriched, form, "porcentajeMejoraEnergeticaAcreditado", "e12_ahorroEnergiaPrimariaNoRenovable");
        enriched.put("porcentajeSubvencionable", "40%"); // Generalmente 40% en L3
        applyMapping(enriched, form, "importeSubvencionResultante", "importeSubvencion");

        // Textos opcionales
        enriched.put("textoOpcional1", "la mejora de la eficiencia energética de la vivienda habitual");
    }

    private void applyObraMassol(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "potenciaInstalacion", "e2_potenciaPicoGenerador");
        enriched.put("direccionCompleta", buildDireccionCompleta(form));
        applyMapping(enriched, form, "referenciaCatastral", "e3_referenciaCatastral");

        applyMapping(enriched, form, "diaInicio", "diaInicio");
        applyMapping(enriched, form, "mesInicio", "mesInicio");
        applyMapping(enriched, form, "anioInicio", "anioInicio");

        applyMapping(enriched, form, "diaFirma", "diaFirmaJustificacion");
        applyMapping(enriched, form, "mesFirma", "mesFirmaJustificacion");
        applyMapping(enriched, form, "anioFirma", "anioFirmaJustificacion");

        String potBomba = getString(form, "potenciaBombaCalle");
        enriched.put("aerotermia", potBomba != null && !potBomba.isEmpty());
    }

    private void applyCertificadoPedidosContratos(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "personaBeneficiaria", "apellidosNombre");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));
        enriched.put("direccion", buildDireccionCompleta(form));
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMappingWithFallback(enriched, form, "expediente", "expedienteEco", "expediente", "numeroExpediente");
        putIfAbsent(enriched, "expediente", "");

        applyMapping(enriched, form, "diaFirma", "diaFirmaJustificacion");
        applyMapping(enriched, form, "mesFirma", "mesFirmaJustificacion");
        applyMapping(enriched, form, "anioFirma", "anioFirmaJustificacion");

        for (int i = 1; i <= 5; i++) {
            applyMapping(enriched, form, "pedido" + i + "Concepto", "pedido" + i + "Concepto");
            applyMapping(enriched, form, "pedido" + i + "Proveedor", "pedido" + i + "Proveedor");
            applyMapping(enriched, form, "pedido" + i + "IdOferta", "pedido" + i + "IdOferta");
            applyMapping(enriched, form, "pedido" + i + "FechaOferta", "pedido" + i + "FechaOferta");
            applyMapping(enriched, form, "pedido" + i + "ImporteOferta", "pedido" + i + "ImporteOferta");
            applyMapping(enriched, form, "pedido" + i + "IdPedido", "pedido" + i + "IdPedido");
            applyMapping(enriched, form, "pedido" + i + "FechaPedido", "pedido" + i + "FechaPedido");
            applyMapping(enriched, form, "pedido" + i + "ImportePedido", "pedido" + i + "ImportePedido");
        }
    }

    private void applyAutorizacionRepresentacion(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "autorizante", "apellidosNombre");
        enriched.put("dniAutorizante", cleanDni(getString(form, "nifCif")));

        enriched.put("domicilioAutorizante", buildDireccionCompleta(form));
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");

        applyMapping(enriched, form, "nombreRepresentante", "representante");
        enriched.put("dniRepresentante", cleanDni(getString(form, "dniRepresentante")));

        applyMapping(enriched, form, "dia", "diaAceptacion");
        applyMapping(enriched, form, "mes", "mesAceptacion");
        applyMapping(enriched, form, "anio", "anioAceptacion");
    }

    private void applyDeclaracionNoGeneracionRcds(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "promotor", "apellidosNombre");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));

        enriched.put("domicilio", buildDireccionCompleta(form));

        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");

        putIfAbsent(enriched, "tecnico", TECNICO_NOMBRE);
        putIfAbsent(enriched, "nifTecnico", TECNICO_NIF);
    }

    private void applyAutorizacionComunicacion(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "solicitante", "apellidosNombre");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));

        applyMapping(enriched, form, "expediente", "expedienteEco");

        // Datos del Representante (solo si existe)
        String rep = getString(form, "representante");
        if (rep != null && !rep.trim().isEmpty()) {
            applyMapping(enriched, form, "nombreRepresentante", "representante");
            enriched.put("dniRepresentante", cleanDni(getString(form, "dniRepresentante")));
            applyMapping(enriched, form, "calidad", "representanteCargo");
            putIfAbsent(enriched, "calidad", "Representante de la sociedad");
        } else {
            enriched.put("nombreRepresentante", "");
            enriched.put("dniRepresentante", "");
            enriched.put("calidad", "");
        }

        // Sección 2: Lugar y medio de notificación (mapeo ultra-robusto)
        String rawCalle = getString(form, "emplazamientoCalle");
        if (rawCalle.isEmpty()) rawCalle = getString(form, "direccion");
        
        String tipoVia = getString(form, "emplazamientoTipoVia");
        if (tipoVia.isEmpty()) tipoVia = getString(form, "tipo_via");
        
        String nombreVia = rawCalle;

        // Lógica inteligente: Si el tipo está vacío pero la calle empieza por "Calle", "Avda", etc.
        if (tipoVia.isEmpty() && !rawCalle.isEmpty()) {
            String[] partes = rawCalle.split(" ", 2);
            String primeraPalabra = partes[0].toLowerCase();
            if (primeraPalabra.equals("calle") || primeraPalabra.equals("avda") || 
                primeraPalabra.equals("avenida") || primeraPalabra.equals("plaza") ||
                primeraPalabra.equals("pso") || primeraPalabra.equals("paseo")) {
                tipoVia = partes[0];
                if (partes.length > 1) nombreVia = partes[1];
            }
        }

        enriched.put("tipoVia", tipoVia);
        enriched.put("nombreVia", nombreVia);

        applyMappingWithFallback(enriched, form, "numero", "numero", "emplazamientoNumero", "num");
        applyMappingWithFallback(enriched, form, "letra", "letra", "emplazamientoLetra");
        applyMapping(enriched, form, "km", "km");
        applyMapping(enriched, form, "bloque", "bloque");
        applyMapping(enriched, form, "portal", "portal");
        applyMapping(enriched, form, "escalera", "escalera");
        applyMapping(enriched, form, "planta", "planta");
        applyMapping(enriched, form, "puerta", "puerta");
        applyMapping(enriched, form, "entidad", "entidad");
        
        applyMappingWithFallback(enriched, form, "municipio", "municipio", "localidad", "localidadEmplazamiento", "ciudad");
        applyMappingWithFallback(enriched, form, "provincia", "provincia", "provinciaEmplazamiento");
        applyMappingWithFallback(enriched, form, "codigoPostal", "codigoPostal", "codigoPostalEmplazamiento", "cp");
        putIfAbsent(enriched, "pais", "ESPAÑA");

        // Contacto (solo móvil)
        // El móvil suele venir en 'telefono' o 'telefonos' en tu base de datos
        
        // Contacto y Email con refuerzo máximo
        applyMappingWithFallback(enriched, form, "telefonoMovil", "telefono", "telefonos", "telefono_movil", "movil", "telefonoRepresentante", "telefono_contacto");
        applyEmailMapping(enriched, form, "correoElectronico");

        applyMapping(enriched, form, "dia", "diaAceptacion");
        applyMapping(enriched, form, "mes", "mesAceptacion");
        applyMapping(enriched, form, "anio", "anioAceptacion");
    }

    private void applyCertificadoAdecuacion(Map<String, Object> enriched, Map<String, Object> form) {
        applyCertificadoSolidez(enriched, form);
        applyMapping(enriched, form, "expediente", "expedienteEco");
        putIfAbsent(enriched, "usoDestino", "Producción de energía eléctrica");
        applyMapping(enriched, form, "usoDestino", "usoDestino");
    }

    private void applyCie(Map<String, Object> enriched, Map<String, Object> form) {
        // Datos del Titular
        applyMapping(enriched, form, "apellidosNombre", "apellidosNombre");
        enriched.put("nifCif", cleanDni(getString(form, "nifCif")));
        
        // Domicilio con número
        String calleTitular = getString(form, "emplazamientoCalle");
        String numTitular = getString(form, "numero");
        enriched.put("domicilio", (calleTitular != null ? calleTitular : "") + (numTitular != null ? " " + numTitular : ""));
        
        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "telefono", "telefono");
        applyEmailMapping(enriched, form, "correoElectronico");
        
        // Emplazamiento
        applyMapping(enriched, form, "emplazamientoCalle", "emplazamientoCalle");
        applyMapping(enriched, form, "numero", "numero");
        applyMapping(enriched, form, "bloque", "bloque");
        applyMapping(enriched, form, "portal", "portal");
        applyMapping(enriched, form, "escalera", "escalera");
        applyMapping(enriched, form, "piso", "planta");
        applyMapping(enriched, form, "puerta", "puerta");
        applyMapping(enriched, form, "codigoPostalEmplazamiento", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "localidadEmplazamiento", "localidadEmplazamiento");
        applyMapping(enriched, form, "provinciaEmplazamiento", "provinciaEmplazamiento");

        // Datos Técnicos y Verificación
        enriched.put("nifInstalador", cleanDni("28.818.007-L"));
        
        // Potencia del Inversor para "Derivación individual - Potencia prevista"
        applyMappingWithFallback(enriched, form, "potenciaContratada", "e2_potenciaNominalInversor", "potenciaACInversor", "potenciaInstalacion");
        
        applyMapping(enriched, form, "cups", "cups");
        putIfAbsent(enriched, "usoDestino", "Producción de energía eléctrica");
        putIfAbsent(enriched, "resistenciaTierra", "20");
        
        // Mapeo de fase para checkboxes con fallbacks
        applyMappingWithFallback(enriched, form, "fase", "e2_tipoConexionRed1", "fase");
        applyMapping(enriched, form, "tipoConexionRed", "e2_tipoConexionRed1");
        
        // Tipo de instalación (Nueva, Ampliación, etc.)
        applyMappingWithFallback(enriched, form, "tipoInstalacion", "instalacion", "tipoInstalacion");
        
        // Tensión de suministro
        applyMapping(enriched, form, "tensionSuministro", "e2_relacionTensionInversor");
        
        // Datos del Director de Obra (Eduardo Rivera por defecto)
        putIfAbsent(enriched, "directorDeObra", "Eduardo Rivera Cabezas");
        putIfAbsent(enriched, "titulacion", "Ingeniero Industrial");
        putIfAbsent(enriched, "colegioOficial", "COIIAOC");
        putIfAbsent(enriched, "numeroColegiado", "4654");

        // Fecha
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");

        // Especial para el recuadro del CIE: Últimos 2 dígitos del año
        String anioValue = getString(form, "anio");
        if (anioValue != null && anioValue.length() >= 2) {
            enriched.put("terminacioAnual", anioValue.substring(anioValue.length() - 2));
        } else {
            enriched.put("terminacioAnual", anioValue);
        }
    }

    private void applyCertificadoDireccionObra(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "promotor", "apellidosNombre");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));
        enriched.put("nifCif", cleanDni(getString(form, "nifCif")));

        enriched.put("emplazamiento", buildDireccionCompleta(form));
    }

    private void applyDeclaracionDireccionObra(Map<String, Object> enriched, Map<String, Object> form) {
        // Datos del Técnico (Fijos para Eduardo Rivera)
        putIfAbsent(enriched, "nombreApellidosEduardoFijo", "Eduardo Rivera Cabezas");
        putIfAbsent(enriched, "nifEduardoFijo", cleanDni("28.818.007-L"));
        putIfAbsent(enriched, "tipoViaFijo", "Calle");
        putIfAbsent(enriched, "nombreViaFijo", "El Peñon");
        putIfAbsent(enriched, "numeroViaFijo", "5");
        putIfAbsent(enriched, "paisFijo", "España");
        putIfAbsent(enriched, "provinciaFijo", "Sevilla");
        putIfAbsent(enriched, "municipioFijo", "Tomares");
        putIfAbsent(enriched, "codigoPostalFijo", "41940");
        putIfAbsent(enriched, "titulacionFijo", "Ingeniero Industrial");
        putIfAbsent(enriched, "especialidadFijo", "Mecánica");
        putIfAbsent(enriched, "universidadFijo", "Universidad de Sevilla");
        putIfAbsent(enriched, "colegioFijo", "COIIAOC");
        putIfAbsent(enriched, "numeroColegiadoFijo", "4654");
        putIfAbsent(enriched, "fraseFija1", "Dirección técnica de instalación fotovoltaica de ");
        putIfAbsent(enriched, "fraseFija2", "Certificado de direccion de obra de instalacion de equipos");
        putIfAbsent(enriched, "nombreFirma", "Eduardo Rivera Cabezas");

        // Mapeos Dinámicos
        applyMapping(enriched, form, "potenciaFrase1", "potenciaProyecto");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "razonSocial", "nombreCubierta");
        applyMapping(enriched, form, "provinciaSelect1", "provinciaEmplazamiento");
        applyMapping(enriched, form, "provinciaSelect2", "provinciaEmplazamiento");
        
        // Mapeos adicionales por si acaso
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");
    }

    private void applyDeclaracionTecnicoCompetente(Map<String, Object> enriched, Map<String, Object> form) {
        // Datos del Técnico (Fijos para Eduardo Rivera)
        putIfAbsent(enriched, "nombreApellidosEduardoFijo", "Eduardo Rivera Cabezas");
        putIfAbsent(enriched, "nifEduardoFijo", cleanDni("28.818.007-L"));
        putIfAbsent(enriched, "tipoViaFijo", "Calle");
        putIfAbsent(enriched, "nombreViaFijo", "El Peñon");
        putIfAbsent(enriched, "numeroViaFijo", "5");
        putIfAbsent(enriched, "paisFijo", "España");
        putIfAbsent(enriched, "provinciaFijo", "Sevilla");
        putIfAbsent(enriched, "municipioFijo", "Tomares");
        putIfAbsent(enriched, "codigoPostalFijo", "41940");
        putIfAbsent(enriched, "titulacionFijo", "Ingeniero Industrial");
        putIfAbsent(enriched, "especialidadFijo", "Mecánica");
        putIfAbsent(enriched, "universidadFijo", "Universidad de Sevilla");
        putIfAbsent(enriched, "colegioFijo", "COIIAOC");
        putIfAbsent(enriched, "numeroColegiadoFijo", "4654");
        putIfAbsent(enriched, "fraseFija1", "Elaboracion de proyecto electrico de instalacion solar fotovoltaica de ");
        putIfAbsent(enriched, "fraseFija2", "Proyecto de ejecuccion de instalacion solar fotovoltaica de ");
        putIfAbsent(enriched, "nombreFirma", "Eduardo Rivera Cabezas");

        // Mapeos Dinámicos
        applyMapping(enriched, form, "potenciaFrase1", "potenciaProyecto");
        applyMapping(enriched, form, "potenciaFrase2", "potenciaProyecto");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "razonSocial", "nombreCubierta");
        applyMapping(enriched, form, "provinciaSelect1", "provinciaEmplazamiento");
        applyMapping(enriched, form, "provinciaSelect2", "provinciaEmplazamiento");

        // Mapeos de fecha
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");
    }

    private void applyDocumentoUltimaPagina(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "dia", "diaAceptacion");
        applyMapping(enriched, form, "mes", "mesAceptacion");
        applyMapping(enriched, form, "anio", "anioAceptacion");
    }

    // =========================================================================
    // MEMORIA TÉCNICA - LÓGICA COMÚN (con batería, sin batería, trifásica, aislada)
    // =========================================================================
    private void applyMemoriaTecnicaCommon(Map<String, Object> enriched, Map<String, Object> form, String templateId) {
        // --- Títulos específicos según la variante ---
        if (templateId.contains("sin-bateria")) {
            putIfAbsent(enriched, "tituloe2", "Generación Fotovoltaica sin acumulación");
        } else if (templateId.contains("con-bateria")) {
            putIfAbsent(enriched, "tituloe2", "Generación Fotovoltaica con acumulación");
        } else if (templateId.contains("punto-recarga")) {
            putIfAbsent(enriched, "tituloe2", "Punto de recarga de vehículo eléctrico");
            applyMemoriaTecnicaPuntoRecarga(enriched, form);
        } else {
            putIfAbsent(enriched, "tituloe2", "Generación Fotovoltaica");
        }

        // --- defaultData: Sección C - datos fijos del técnico instalador ---
        putIfAbsent(enriched, "nombreTecnicoInstalador", TECNICO_NOMBRE);
        putIfAbsent(enriched, "numeroCertificadoInstalador", TECNICO_COLEGIO_CORTO);
        putIfAbsent(enriched, "numeroInstaladorEmpresa", EMPRESA_NUMERO_EMPRESA);
        putIfAbsent(enriched, "domicilioTecnico", TECNICO_DOMICILIO);
        putIfAbsent(enriched, "numeroTecnico", TECNICO_NUMERO);
        putIfAbsent(enriched, "localidadTecnico", TECNICO_LOCALIDAD);
        putIfAbsent(enriched, "codigoPostalTecnico", TECNICO_CP);
        putIfAbsent(enriched, "telefonoTecnico", TECNICO_TELEFONO);

        // --- fieldMapping: dirección concatenada (Sección A / DOMICILIO) ---
        // IMPORTANTE: la plantilla HTML usa 'direccionCompleta' en th:text
        String domicilio = buildDireccionCompleta(form);
        putIfAbsent(enriched, "domicilio", domicilio);
        putIfAbsent(enriched, "direccionCompleta", domicilio);

        // --- fieldMapping: mapeos simples de campos DB → nombre plantilla ---
        String nifLimpio = cleanDni(getString(form, "nifCif"));
        enriched.put("dni", nifLimpio);
        enriched.put("nifCif", nifLimpio);
        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "telefono", "telefono");
        applyMapping(enriched, form, "telefonoMovil", "telefono");
        applyMapping(enriched, form, "correoElectronico", "correoElectronicoEmplazamiento");
        applyMapping(enriched, form, "piso", "planta");

        // --- fieldMapping: Sección E2.1 Conexión a la Red ---
        applyMapping(enriched, form, "potenciaNominalInversores", "e2_potenciaNominalInversores");
        applyMapping(enriched, form, "tipoConexionRed", "e2_tipoConexionRed1");

        // --- fieldMapping: Sección E2.2 Módulo Fotovoltaico ---
        applyMapping(enriched, form, "tecnologiaCelulaModulo", "e2_tecnologiaCelulaModulo");
        applyMapping(enriched, form, "marcaModeloModulo", "e2_marcaModeloModulo");
        applyMapping(enriched, form, "potenciaPicoModulo", "e2_potenciaPicoModulo");
        applyMapping(enriched, form, "toncModulo", "e2_toncModulo");

        // --- fieldMapping: Sección E2.3 Generador ---
        applyMapping(enriched, form, "potenciaPicoGenerador", "e2_potenciaPicoGenerador");
        
        // Conversión a kW para la plantilla (ej: 4800 -> 4,8)
        Object potPicoRaw = form.get("e2_potenciaPicoGenerador");
        if (potPicoRaw != null) {
            try {
                double potKw = Double.parseDouble(potPicoRaw.toString());
                if (potKw > 50) { // Si es mayor de 50, asumimos que son W y pasamos a kW
                    potKw = potKw / 1000.0;
                }
                enriched.put("potenciaPicoGeneradorKW", String.format("%.2f", potKw).replace(".", ","));
            } catch (Exception e) {
                enriched.put("potenciaPicoGeneradorKW", potPicoRaw.toString());
            }
        }
        applyMapping(enriched, form, "tensionVpmpGenerador", "e2_tensionVpmpGenerador");
        applyMapping(enriched, form, "orientacionGenerador", "e2_orientacionGenerador");
        applyMapping(enriched, form, "inclinacionGenerador", "e2_inclinacionGenerador");
        applyMapping(enriched, form, "totalModulos", "e2_totalModulos");
        applyMapping(enriched, form, "modulosEnSerie", "e2_modulosEnSerie");

        // --- fieldMapping: Sección E2.4 Inversor ---
        applyMapping(enriched, form, "marcaModeloInversor", "e2_marcaModeloInversor");
        applyMapping(enriched, form, "potenciaACInversor", "e2_potenciaNominalInversor");
        applyMapping(enriched, form, "tensionNominalInversor", "e2_relacionTensionInversor");
        applyMapping(enriched, form, "tipoConexionInversor", "e2_tipoConexionRed1");
        applyMapping(enriched, form, "marcaModeloInversor2", "e2_marcaModeloInversor2");
        applyMapping(enriched, form, "potenciaACInversor2", "e2_potenciaNominalInversor2");
        applyMapping(enriched, form, "tensionNominalInversor2", "e2_relacionTensionInversor2");
        applyMapping(enriched, form, "tipoConexionInversor2", "e2_tipoConexionRed2");

        // --- fieldMapping: Sección E2.5 Baterías ---
        applyMapping(enriched, form, "marcaModeloBateria", "e2_marcaModelo");
        applyMapping(enriched, form, "tipoBateria", "e2_tipoDeBateria");
        applyMapping(enriched, form, "tensionNominalBateria", "e2_tensionNominal");
        applyMapping(enriched, form, "profundidadDescargaBateria", "e2_profundidadDescarga");
        applyMapping(enriched, form, "tensionMaximaBateria", "e2_tensionMaxima");
        applyMapping(enriched, form, "tensionMinimaBateria", "e2_tensionMinima");
        applyMapping(enriched, form, "energiaTotalBateria", "e2_energiaTotal");
        applyMapping(enriched, form, "potenciaMaximaSalidaBateria", "e2_potenciaMaximaSalida");
        applyMapping(enriched, form, "maximoPicoPotenciaBateria", "e2_maximoPicoDePotencia");

        // --- fieldMapping: Sección E2.5.1 Protecciones Externas ---
        applyMapping(enriched, form, "intensidadInterruptorGeneral", "e2_intensidadNominalInterruptor");
        applyMapping(enriched, form, "poderCorteInterruptor", "e2_poderCorteInterruptor");

        // --- fieldMapping: Sección G Circuitos ---
        applyMapping(enriched, form, "potenciaBateriaInversor", "g_bateriaDiRectaInversorPotencia");
        applyMapping(enriched, form, "longitudBateriaInversor", "g_bateriaDiRectaInversorLongitud");
        applyMapping(enriched, form, "materialBateriaInversor", "g_bateriaDiRectaInversorSeccion");
        
        applyMapping(enriched, form, "potenciaGeneradorInversorDirecto", "g_generadorDirectoInversorPotencia");
        applyMapping(enriched, form, "longitudGeneradorInversorDirecto", "g_generadorDirectoInversorLongitud");
        applyMapping(enriched, form, "materialGeneradorInversorDirecto", "g_generadorDirectoInversorSeccion");
        
        // Mapeos adicionales para compatibilidad con MemoriaTecnica.html (Sección G)
        applyMapping(enriched, form, "longitud_generador_inversor", "g_generadorDirectoInversorLongitud");
        applyMapping(enriched, form, "seccion_generador_inversor", "g_generadorDirectoInversorSeccion");
        applyMapping(enriched, form, "intensidad_generador_inversor", "g_generadorDirectoInversorIntensidad");
        applyMapping(enriched, form, "caida_generador_inversor", "g_generadorDirectoInversorCaida");

        // Batería - Inversor
        applyMapping(enriched, form, "longitud_bateria_inversor", "g_bateriaDiRectaInversorLongitud");
        applyMapping(enriched, form, "seccion_bateria_inversor", "g_bateriaDiRectaInversorSeccion");
        applyMapping(enriched, form, "intensidad_bateria_inversor", "g_bateriaDiRectaInversorIntensidad");
        applyMapping(enriched, form, "caida_bateria_inversor", "g_bateriaDiRectaInversorCaida");

        // Inversor - Red
        applyMapping(enriched, form, "longitud_inversor_red", "g_inversorRedLongitud");
        applyMapping(enriched, form, "seccion_inversor_red", "g_inversorRedSeccion");
        applyMapping(enriched, form, "intensidad_inversor_red", "g_inversorRedIntensidad");
        applyMapping(enriched, form, "caida_inversor_red", "g_inversorRedCaida");
        
        applyMapping(enriched, form, "potenciaSalidaInversorRed", "g_inversorRedPotencia");
        applyMapping(enriched, form, "longitudSalidaInversorRed", "g_inversorRedLongitud");
        applyMapping(enriched, form, "materialSalidaInversorRed", "g_inversorRedSeccion");

        // --- fieldMapping: Sección H y I ---
        applyMapping(enriched, form, "esquemaUnifilar", "h_esquemaUnifilar");
        applyMapping(enriched, form, "planoEmplazamiento", "otros_imagenPlanoEmplazamiento");

        // --- defaultData: valores por defecto para protecciones y técnicos ---
        putIfAbsent(enriched, "medidaContactosDirectos", "Conductores aislados y canaletas");
        putIfAbsent(enriched, "medidaContactosIndirectos", "Interruptor diferencial");
        putIfAbsent(enriched, "parteInstalacionIndirectos", "CGMP");
        putIfAbsent(enriched, "medidaSobretensiones", "Descargador");
        putIfAbsent(enriched, "parteInstalacionSobretensiones", "CGMP");
        putIfAbsent(enriched, "medidaPuntoCaliente", "3 Diodos bypass");
        putIfAbsent(enriched, "parteInstalacionPuntoCaliente", "Módulo FV");
        putIfAbsent(enriched, "intensidadInterruptorGeneral", "25");
        putIfAbsent(enriched, "poderCorteInterruptor", "6");
        putIfAbsent(enriched, "tecnologiaCelulaModulo", "Monocristalino -PERC- doble célula");
        putIfAbsent(enriched, "toncModulo", "45ºC");
        putIfAbsent(enriched, "intensidadIpmpGenerador", "10,84");
        putIfAbsent(enriched, "ramasEnParalelo", "1");

        // --- defaultData: modalidades (siempre marcadas por defecto) ---
        putIfAbsent(enriched, "modalidadBasicaM1", true);
        putIfAbsent(enriched, "modalidadBasicaM2", true);
        putIfAbsent(enriched, "modalidadBasicaM3", true);
        putIfAbsent(enriched, "modalidadEspecialistaM4", true);
        putIfAbsent(enriched, "modalidadEspecialistaM5", true);
        putIfAbsent(enriched, "modalidadEspecialistaM6", true);
        putIfAbsent(enriched, "modalidadEspecialistaM7", true);
        putIfAbsent(enriched, "modalidadEspecialistaM8", true);
        putIfAbsent(enriched, "modalidadEspecialistaM9", true);
    }

    // =========================================================================
    // MEMORIA TÉCNICA PUNTO DE RECARGA - valores adicionales Sección E
    // =========================================================================
    private void applyMemoriaTecnicaPuntoRecarga(Map<String, Object> enriched, Map<String, Object> form) {
        // defaultData específico de memoriaTecnicaPuntoRecargaConfig.defaultData
        putIfAbsent(enriched, "cargadorMarca", "SMA EV");
        putIfAbsent(enriched, "trifasica", true);
        putIfAbsent(enriched, "potenciaInstalada", "22");
        putIfAbsent(enriched, "potenciaPrevisita", "22");
        putIfAbsent(enriched, "tipoInstalacionRecarga", "INTERIOR");
        putIfAbsent(enriched, "modoCarga", "Modo 3 (IEC 61851-)");
        putIfAbsent(enriched, "tipoConector", "2");
        putIfAbsent(enriched, "descargadorSobretensiones", "Sí");
        putIfAbsent(enriched, "puestaATierra", "Según normas");
        putIfAbsent(enriched, "longitudCuadroPrincipal", "10");
        putIfAbsent(enriched, "materialConductor", "6");
        putIfAbsent(enriched, "intensidadAdmisible", "49");
        putIfAbsent(enriched, "caidaTension", "< 1,5 %");
    }

    // =========================================================================
    // DECLARACIÓN DE HABILITACIÓN PROFESIONAL
    // =========================================================================
    private void applyDeclaracionHabilitacion(Map<String, Object> enriched, Map<String, Object> form) {
        putIfAbsent(enriched, "nombreProfesional", TECNICO_NOMBRE);
        putIfAbsent(enriched, "nifProfesional", TECNICO_NIF);
        putIfAbsent(enriched, "profesionTitulo", "Ingeniero Industrial");
        putIfAbsent(enriched, "numeroColegiado", TECNICO_NUMERO_COLEGIADO);
        putIfAbsent(enriched, "nombreColegio", TECNICO_COLEGIO);
        putIfAbsent(enriched, "domicilioProfesional", "Calle El Peñón 5");
        putIfAbsent(enriched, "codigoPostalProfesional", "41940");
        putIfAbsent(enriched, "localidadProfesional", "Tomares");
        putIfAbsent(enriched, "provinciaProfesional", "Sevilla");
        putIfAbsent(enriched, "ciudadFirma", "Jerez de la Frontera");

        // fieldMapping: ciudadFirma puede venir de la BD
        applyMapping(enriched, form, "ciudadFirma", "localidadEmplazamiento");
    }

    // =========================================================================
    // CERTIFICADOS DE SOLIDEZ Y SEGURIDAD
    // =========================================================================
    private void applyCertificadoSolidez(Map<String, Object> enriched, Map<String, Object> form) {
        String direccion = buildDireccionCompleta(form);
        putIfAbsent(enriched, "direccion", direccion);
        putIfAbsent(enriched, "direccionCompleta", direccion);

        // Mapeo e Identificación
        enriched.put("nif", cleanDni(getString(form, "nifCif")));

        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "numModulos", "e2_totalModulos");
        applyMapping(enriched, form, "potencia", "e2_potenciaPicoModulo");
        applyMapping(enriched, form, "marcaModelo", "e2_marcaModeloModulo");
        applyMapping(enriched, form, "ciudadFirma", "localidadEmplazamiento");
        applyMapping(enriched, form, "foto1", "otros_foto1");
        applyMapping(enriched, form, "foto2", "otros_foto2");
    }

    // =========================================================================
    // PLANOS DE SITUACIÓN, EMPLAZAMIENTO Y CUBIERTA
    // =========================================================================
    private void applyPlanosSituacionEmplazamiento(Map<String, Object> enriched, Map<String, Object> form) {
        // Mapeos básicos de titular y ubicación
        applyMapping(enriched, form, "promotor", "apellidosNombre");
        enriched.put("nif", cleanDni(getString(form, "nifCif")));

        String direccion = buildDireccionCompleta(form);
        putIfAbsent(enriched, "direccion", direccion);

        applyMapping(enriched, form, "numero", "numero");
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");
        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "referenciaCatastral", "referenciaCatastral");

        // Datos técnicos de la instalación
        applyMapping(enriched, form, "totalModulos", "e2_totalModulos");
        applyMapping(enriched, form, "potenciaModulos", "e2_potenciaPicoModulo");
        applyMapping(enriched, form, "potenciaPicoGenerador", "e2_potenciaPicoGenerador");
        applyMapping(enriched, form, "disposicionModulos", "orientacionGenerador");
        applyMapping(enriched, form, "tipoInstalacion", "tipoInstalacionRecarga");

        // Variables de cabecera y descriptivas (Defaults de Vue)
        putIfAbsent(enriched, "pse_tipo", "Instalación Solar Fotovoltaica");

        // Conversión de potencia a kW si es necesario (ej: 6050 -> 6.05)
        Object potPicoRaw = enriched.get("potenciaPicoGenerador");
        if (potPicoRaw != null) {
            try {
                double potKw = Double.parseDouble(potPicoRaw.toString()) / 1000.0;
                enriched.put("pse_potencia", String.format("%.2f", potKw));
            } catch (Exception e) {
                enriched.put("pse_potencia", potPicoRaw.toString());
            }
        } else {
            putIfAbsent(enriched, "pse_potencia", "0");
        }

        putIfAbsent(enriched, "pse_descripcion", "para Autoconsumo");

        // Fechas de firma/aceptación (según logs: [dia], [mes], [anio])
        applyMapping(enriched, form, "pse_dia", "dia");
        applyMapping(enriched, form, "pse_mes", "mes");
        applyMapping(enriched, form, "pse_anio", "anio");

        // Fallbacks para fecha
        putIfAbsent(enriched, "pse_dia", "___");
        putIfAbsent(enriched, "pse_mes", "___");
        putIfAbsent(enriched, "pse_anio", "___");
    }

    private void applyEstudioSeguridadSalud(Map<String, Object> enriched, Map<String, Object> form) {
        // Mapeo de props de Vue a campos de BD
        applyMapping(enriched, form, "nombre", "apellidosNombre");
        enriched.put("dni", cleanDni(getString(form, "nifCif")));
        applyMapping(enriched, form, "referenciaCatastral", "referenciaCatastral");
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "numero", "numero");

        String direccionFull = buildDireccionCompleta(form);
        putIfAbsent(enriched, "direccion", direccionFull);

        // Fechas
        applyMapping(enriched, form, "dia", "dia");
        applyMapping(enriched, form, "mes", "mes");
        applyMapping(enriched, form, "anio", "anio");

        // Datos técnicos
        applyMapping(enriched, form, "potenciaModulos", "e2_potenciaPicoGenerador");
        applyMapping(enriched, form, "potencia", "e2_potenciaNominalInversores");

        // Si potenciaModulos (Wp) es muy grande, tal vez convenga mostrarlo así
        putIfAbsent(enriched, "potenciaModulos", "0");
        putIfAbsent(enriched, "potencia", "0");

        // Presupuesto (como no se conoce la clave, lo dejamos para que se pueda
        // completar manual o buscamos 'presupuesto')
        applyMapping(enriched, form, "presupuesto", "presupuestoTotal");
        putIfAbsent(enriched, "presupuesto", "___");

        // Ciudad para la firma
        putIfAbsent(enriched, "ciudad", enriched.get("localidad"));
    }

    // =========================================================================
    // MAPEO COMÚN (para plantillas sin configuración específica)
    // =========================================================================
    private void applyCommonFieldMapping(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "localidad", "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia", "provinciaEmplazamiento");
        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "ciudadFirma", "localidadEmplazamiento");
        applyEmailMapping(enriched, form, "correoElectronico");
    }

    /**
     * Mapeo de email ultra-robusto con todos los fallbacks detectados en la BD.
     */
    private void applyEmailMapping(Map<String, Object> enriched, Map<String, Object> form, String targetKey) {
        applyMappingWithFallback(enriched, form, targetKey, 
            "correoElectronico", "email", "emailEmplazamiento", "email_emplazamiento", 
            "correoElectronicoEmplazamiento", "email_titular", "e_mail", "e-mail", 
            "mail", "correo", "email_contacto", "emailInstalacion", "emailRepresentante"
        );
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    /**
     * Construye la dirección completa concatenando los campos de emplazamiento,
     * replicando la lógica de los `fieldMapping` del documents.js de Vue.
     */
    private String buildDireccionCompleta(Map<String, Object> form) {
        StringBuilder sb = new StringBuilder();
        appendIfNotEmpty(sb, getString(form, "emplazamientoCalle"));
        appendIfNotEmpty(sb, getString(form, "numero"));
        String bloque = getString(form, "bloque");
        if (!bloque.isEmpty())
            sb.append(" Bloque ").append(bloque);
        String escalera = getString(form, "escalera");
        if (!escalera.isEmpty())
            sb.append(" Escalera ").append(escalera);
        String planta = getString(form, "planta");
        if (!planta.isEmpty())
            sb.append(" Planta ").append(planta);
        String puerta = getString(form, "puerta");
        if (!puerta.isEmpty())
            sb.append(" Puerta ").append(puerta);
        return sb.toString().trim();
    }

    /**
     * Copia el valor de `sourceKey` del formulario BD a `targetKey` en el mapa
     * enriquecido,
     * pero SÓLO si la fuente tiene valor. La clave destino siempre se sobreescribe
     * con el
     * valor de BD cuando este existe (el BD manda sobre el default).
     */
    private void applyMapping(Map<String, Object> enriched, Map<String, Object> form,
            String targetKey, String sourceKey) {
        Object sourceValue = form.get(sourceKey);
        if (sourceValue != null && !sourceValue.toString().isEmpty()) {
            enriched.put(targetKey, sourceValue);
        }
    }

    /**
     * Intenta mapear desde varias claves de origen hasta encontrar una con valor.
     */
    private void applyMappingWithFallback(Map<String, Object> enriched, Map<String, Object> form,
            String targetKey, String... sourceKeys) {
        for (String key : sourceKeys) {
            Object val = form.get(key);
            if (val != null && !val.toString().trim().isEmpty()) {
                enriched.put(targetKey, val);
                return;
            }
        }
    }

    /**
     * Limpia el DNI eliminando puntos, espacios y guiones.
     */
    private String cleanDni(String dni) {
        if (dni == null)
            return "";
        // Eliminamos puntos, espacios y guiones.
        return dni.replace(".", "").replace(" ", "").replace("-", "").trim();
    }

    /**
     * Sólo inserta el valor por defecto si la clave no existe o está vacía en el
     * mapa.
     */
    private void putIfAbsent(Map<String, Object> map, String key, Object defaultValue) {
        Object existing = map.get(key);
        if (existing == null || existing.toString().isEmpty()) {
            map.put(key, defaultValue);
        }
    }

    private String getString(Map<String, Object> form, String key) {
        Object val = form.get(key);
        return (val != null) ? val.toString() : "";
    }

    private void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            if (sb.length() > 0)
                sb.append(" ");
            sb.append(value);
        }
    }

    /**
     * Limpia y sanitiza recursivamente un mapa de datos para corregir caracteres especiales corruptos.
     */
    private Map<String, Object> sanitizeMap(Map<String, Object> map) {
        if (map == null) return null;
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                sanitized.put(entry.getKey(), sanitizeText((String) value));
            } else if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                sanitized.put(entry.getKey(), sanitizeMap(nestedMap));
            } else {
                sanitized.put(entry.getKey(), value);
            }
        }
        return sanitized;
    }

    /**
     * Corrige caracteres especiales corruptos comunes causados por problemas de codificación.
     */
    private String sanitizeText(String text) {
        if (text == null) return null;
        return text.replace("Ã¡", "á")
                   .replace("Ã©", "é")
                   .replace("Ã­", "í")
                   .replace("Ã³", "ó")
                   .replace("Ãº", "ú")
                   .replace("Ã±", "ñ")
                   .replace("Ã\u0081", "Á")
                   .replace("Ã\u0089", "É")
                   .replace("Ã\u008D", "Í")
                   .replace("Ã\u0093", "Ó")
                   .replace("Ã\u009A", "Ú")
                   .replace("Ã\u0091", "Ñ")
                   .replace("Âº", "º")
                   .replace("Âª", "ª")
                   .trim();
    }

    private double parseDouble(String value) {
        if (value == null || value.isEmpty())
            return 0.0;
        try {
            // Limpiamos posibles símbolos de moneda o espacios
            String clean = value.replace("€", "").replace(" ", "").replace(",", ".");
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
