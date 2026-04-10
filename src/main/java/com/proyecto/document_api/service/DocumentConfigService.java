package com.proyecto.document_api.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que replica la lógica de `defaultData` y `fieldMapping` del archivo
 * `documents.js` del proyecto Vue. Se encarga de:
 * 1. Aplicar valores por defecto (datos del técnico, empresa, etc.) que NO vienen en el JSON de BD.
 * 2. Mapear campos de la BD al nombre que espera la plantilla Thymeleaf.
 *
 * Se llama ANTES de pasar los datos al motor de plantillas.
 */
@Service
public class DocumentConfigService {

    // =========================================================================
    // VALORES FIJOS DEL TÉCNICO (Eduardo Rivera Cabezas)
    // =========================================================================
    private static final String TECNICO_NOMBRE           = "Eduardo Rivera Cabezas";
    private static final String TECNICO_NIF              = "28.818.007-L";
    private static final String TECNICO_NUMERO_COLEGIADO = "4654";
    private static final String TECNICO_COLEGIO          = "Colegio Oficial de Ingenieros Industriales de Andalucía Occidental";
    private static final String TECNICO_COLEGIO_CORTO    = "4654 COIIAOC";
    private static final String TECNICO_DOMICILIO        = "Calle Ebro";
    private static final String TECNICO_NUMERO           = "35";
    private static final String TECNICO_LOCALIDAD        = "Sevilla";
    private static final String TECNICO_CP               = "41.012";
    private static final String TECNICO_TELEFONO         = "629 118 196";
    private static final String EMPRESA_NUMERO_EMPRESA   = "41045500";

    /**
     * Enriquece el `formData` con defaultData y fieldMapping para una plantilla dada.
     *
     * @param templateName Nombre de la plantilla (sin .html)
     * @param formData     Mapa de datos tal como vienen de la base de datos
     * @return Nuevo mapa enriquecido listo para Thymeleaf
     */
    public Map<String, Object> enrich(String templateName, Map<String, Object> formData) {
        Map<String, Object> enriched = new HashMap<>(formData);

        switch (templateName) {
            case "MemoriaTecnicaPuntoRecarga":
                applyMemoriaTecnicaCommon(enriched, formData);
                applyMemoriaTecnicaPuntoRecarga(enriched, formData);
                break;
            case "MemoriaTecnica":
            case "MemoriaTecnicaSinBateria":
            case "MemoriaTecnicaTrifasica":
            case "MemoriaTecnicaAislada":
                applyMemoriaTecnicaCommon(enriched, formData);
                break;
            case "DeclaracionHabilitacionProfesional":
                applyDeclaracionHabilitacion(enriched, formData);
                break;
            case "CertificadoCoplanarTeja":
            case "CertificadoAporticadaTeja":
            case "CertificadoCubiertaPlanaAporticada":
            case "CertificadoChapasGrecadasAporticadas":
            case "CertificadoChapasGrecadasCoplanaria":
            case "CertificadoParamentoVertical":
            case "CertificadoPergolaAporticada":
                applyCertificadoSolidez(enriched, formData);
                break;
            case "PlanosSituacionEmplazamientoCubierta":
                applyPlanosSituacionEmplazamiento(enriched, formData);
                break;
            default:
                applyCommonFieldMapping(enriched, formData);
                break;
        }

        return enriched;
    }

    // =========================================================================
    // MEMORIA TÉCNICA - LÓGICA COMÚN (con batería, sin batería, trifásica, aislada)
    // =========================================================================
    private void applyMemoriaTecnicaCommon(Map<String, Object> enriched, Map<String, Object> form) {

        // --- defaultData: Sección C - datos fijos del técnico instalador ---
        putIfAbsent(enriched, "nombreTecnicoInstalador",     TECNICO_NOMBRE);
        putIfAbsent(enriched, "numeroCertificadoInstalador", TECNICO_COLEGIO_CORTO);
        putIfAbsent(enriched, "numeroInstaladorEmpresa",     EMPRESA_NUMERO_EMPRESA);
        putIfAbsent(enriched, "domicilioTecnico",            TECNICO_DOMICILIO);
        putIfAbsent(enriched, "numeroTecnico",               TECNICO_NUMERO);
        putIfAbsent(enriched, "localidadTecnico",            TECNICO_LOCALIDAD);
        putIfAbsent(enriched, "codigoPostalTecnico",         TECNICO_CP);
        putIfAbsent(enriched, "telefonoTecnico",             TECNICO_TELEFONO);

        // --- fieldMapping: dirección concatenada (Sección A / DOMICILIO) ---
        // IMPORTANTE: la plantilla HTML usa 'direccionCompleta' en th:text
        String domicilio = buildDireccionCompleta(form);
        putIfAbsent(enriched, "domicilio",          domicilio);
        putIfAbsent(enriched, "direccionCompleta",  domicilio);

        // --- fieldMapping: mapeos simples de campos DB → nombre plantilla ---
        applyMapping(enriched, form, "codigoPostal",      "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "localidad",         "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia",         "provinciaEmplazamiento");
        applyMapping(enriched, form, "correoElectronico", "correoElectronicoEmplazamiento");
        applyMapping(enriched, form, "piso",              "planta");

        // --- fieldMapping: Sección E2.1 Conexión a la Red ---
        applyMapping(enriched, form, "potenciaNominalInversores", "e2_potenciaNominalInversores");
        applyMapping(enriched, form, "tipoConexionRed",           "e2_tipoConexionRed1");

        // --- fieldMapping: Sección E2.2 Módulo Fotovoltaico ---
        applyMapping(enriched, form, "tecnologiaCelulaModulo", "e2_tecnologiaCelulaModulo");
        applyMapping(enriched, form, "marcaModeloModulo",      "e2_marcaModeloModulo");
        applyMapping(enriched, form, "potenciaPicoModulo",     "e2_potenciaPicoModulo");
        applyMapping(enriched, form, "toncModulo",             "e2_toncModulo");

        // --- fieldMapping: Sección E2.3 Generador ---
        applyMapping(enriched, form, "potenciaPicoGenerador", "e2_potenciaPicoGenerador");
        applyMapping(enriched, form, "tensionVpmpGenerador",  "e2_tensionVpmpGenerador");
        applyMapping(enriched, form, "orientacionGenerador",  "e2_orientacionGenerador");
        applyMapping(enriched, form, "inclinacionGenerador",  "e2_inclinacionGenerador");
        applyMapping(enriched, form, "totalModulos",           "e2_totalModulos");
        applyMapping(enriched, form, "modulosEnSerie",         "e2_modulosEnSerie");

        // --- fieldMapping: Sección E2.4 Inversor ---
        applyMapping(enriched, form, "marcaModeloInversor",     "e2_marcaModeloInversor");
        applyMapping(enriched, form, "potenciaACInversor",      "e2_potenciaNominalInversor");
        applyMapping(enriched, form, "tensionNominalInversor",  "e2_relacionTensionInversor");
        applyMapping(enriched, form, "tipoConexionInversor",    "e2_tipoConexionRed1");
        applyMapping(enriched, form, "marcaModeloInversor2",    "e2_marcaModeloInversor2");
        applyMapping(enriched, form, "potenciaACInversor2",     "e2_potenciaNominalInversor2");
        applyMapping(enriched, form, "tensionNominalInversor2", "e2_relacionTensionInversor2");
        applyMapping(enriched, form, "tipoConexionInversor2",   "e2_tipoConexionRed2");

        // --- fieldMapping: Sección E2.5 Baterías ---
        applyMapping(enriched, form, "marcaModeloBateria",           "e2_marcaModelo");
        applyMapping(enriched, form, "tipoBateria",                  "e2_tipoDeBateria");
        applyMapping(enriched, form, "tensionNominalBateria",        "e2_tensionNominal");
        applyMapping(enriched, form, "profundidadDescargaBateria",   "e2_profundidadDescarga");
        applyMapping(enriched, form, "tensionMaximaBateria",         "e2_tensionMaxima");
        applyMapping(enriched, form, "tensionMinimaBateria",         "e2_tensionMinima");
        applyMapping(enriched, form, "energiaTotalBateria",          "e2_energiaTotal");
        applyMapping(enriched, form, "potenciaMaximaSalidaBateria",  "e2_potenciaMaximaSalida");
        applyMapping(enriched, form, "maximoPicoPotenciaBateria",    "e2_maximoPicoDePotencia");

        // --- fieldMapping: Sección E2.5.1 Protecciones Externas ---
        applyMapping(enriched, form, "intensidadInterruptorGeneral", "e2_intensidadNominalInterruptor");
        applyMapping(enriched, form, "poderCorteInterruptor",        "e2_poderCorteInterruptor");

        // --- fieldMapping: Sección G Circuitos ---
        applyMapping(enriched, form, "potenciaBateriaInversor",          "g_bateriaDiRectaInversorPotencia");
        applyMapping(enriched, form, "potenciaGeneradorInversorDirecto", "g_generadorDirectoInversorPotencia");
        applyMapping(enriched, form, "potenciaSalidaInversorRed",        "g_inversorRedPotencia");

        // --- fieldMapping: Sección H y I ---
        applyMapping(enriched, form, "esquemaUnifilar",    "h_esquemaUnifilar");
        applyMapping(enriched, form, "planoEmplazamiento", "otros_imagenPlanoEmplazamiento");

        // --- defaultData: valores por defecto para protecciones y técnicos ---
        putIfAbsent(enriched, "medidaContactosDirectos",        "Conductores aislados y canaletas");
        putIfAbsent(enriched, "medidaContactosIndirectos",      "Interruptor diferencial");
        putIfAbsent(enriched, "parteInstalacionIndirectos",     "CGMP");
        putIfAbsent(enriched, "medidaSobretensiones",           "Descargador");
        putIfAbsent(enriched, "parteInstalacionSobretensiones", "CGMP");
        putIfAbsent(enriched, "medidaPuntoCaliente",            "3 Diodos bypass");
        putIfAbsent(enriched, "parteInstalacionPuntoCaliente",  "Módulo FV");
        putIfAbsent(enriched, "intensidadInterruptorGeneral",   "25");
        putIfAbsent(enriched, "poderCorteInterruptor",          "6");
        putIfAbsent(enriched, "tecnologiaCelulaModulo",         "Monocristalino -PERC- doble célula");
        putIfAbsent(enriched, "toncModulo",                     "45ºC");
        putIfAbsent(enriched, "intensidadIpmpGenerador",        "10,84");
        putIfAbsent(enriched, "ramasEnParalelo",                "1");

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
        putIfAbsent(enriched, "cargadorMarca",             "SMA EV");
        putIfAbsent(enriched, "trifasica",                 true);
        putIfAbsent(enriched, "potenciaInstalada",         "22");
        putIfAbsent(enriched, "potenciaPrevisita",         "22");
        putIfAbsent(enriched, "tipoInstalacionRecarga",    "INTERIOR");
        putIfAbsent(enriched, "modoCarga",                 "Modo 3 (IEC 61851-)");
        putIfAbsent(enriched, "tipoConector",              "2");
        putIfAbsent(enriched, "descargadorSobretensiones", "Sí");
        putIfAbsent(enriched, "puestaATierra",             "Según normas");
        putIfAbsent(enriched, "longitudCuadroPrincipal",   "10");
        putIfAbsent(enriched, "materialConductor",         "6");
        putIfAbsent(enriched, "intensidadAdmisible",       "49");
        putIfAbsent(enriched, "caidaTension",              "< 1,5 %");
    }

    // =========================================================================
    // DECLARACIÓN DE HABILITACIÓN PROFESIONAL
    // =========================================================================
    private void applyDeclaracionHabilitacion(Map<String, Object> enriched, Map<String, Object> form) {
        putIfAbsent(enriched, "nombreProfesional",        TECNICO_NOMBRE);
        putIfAbsent(enriched, "nifProfesional",           TECNICO_NIF);
        putIfAbsent(enriched, "profesionTitulo",          "Ingeniero Industrial");
        putIfAbsent(enriched, "numeroColegiado",          TECNICO_NUMERO_COLEGIADO);
        putIfAbsent(enriched, "nombreColegio",            TECNICO_COLEGIO);
        putIfAbsent(enriched, "domicilioProfesional",     "Calle El Peñón 5");
        putIfAbsent(enriched, "codigoPostalProfesional",  "41940");
        putIfAbsent(enriched, "localidadProfesional",     "Tomares");
        putIfAbsent(enriched, "provinciaProfesional",     "Sevilla");
        putIfAbsent(enriched, "ciudadFirma",              "Jerez de la Frontera");

        // fieldMapping: ciudadFirma puede venir de la BD
        applyMapping(enriched, form, "ciudadFirma", "localidadEmplazamiento");
    }

    // =========================================================================
    // CERTIFICADOS DE SOLIDEZ Y SEGURIDAD
    // =========================================================================
    private void applyCertificadoSolidez(Map<String, Object> enriched, Map<String, Object> form) {
        String direccion = buildDireccionCompleta(form);
        putIfAbsent(enriched, "direccion",         direccion);
        putIfAbsent(enriched, "direccionCompleta", direccion);

        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "localidad",    "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia",    "provinciaEmplazamiento");
        applyMapping(enriched, form, "numModulos",   "e2_totalModulos");
        applyMapping(enriched, form, "potencia",     "e2_potenciaPicoModulo");
        applyMapping(enriched, form, "marcaModelo",  "e2_marcaModeloModulo");
        applyMapping(enriched, form, "ciudadFirma",  "localidadEmplazamiento");
        applyMapping(enriched, form, "foto1",        "otros_foto1");
        applyMapping(enriched, form, "foto2",        "otros_foto2");
    }
    
    // =========================================================================
    // PLANOS DE SITUACIÓN, EMPLAZAMIENTO Y CUBIERTA
    // =========================================================================
    private void applyPlanosSituacionEmplazamiento(Map<String, Object> enriched, Map<String, Object> form) {
        // Mapeos básicos de titular y ubicación
        applyMapping(enriched, form, "promotor", "apellidosNombre");
        applyMapping(enriched, form, "nif",      "nifCif");
        
        String direccion = buildDireccionCompleta(form);
        putIfAbsent(enriched, "direccion", direccion);
        
        applyMapping(enriched, form, "numero",             "numero");
        applyMapping(enriched, form, "localidad",          "localidadEmplazamiento");
        applyMapping(enriched, form, "codigoPostal",       "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "provincia",          "provinciaEmplazamiento");
        applyMapping(enriched, form, "referenciaCatastral", "referenciaCatastral");

        // Datos técnicos de la instalación
        applyMapping(enriched, form, "totalModulos",          "e2_totalModulos");
        applyMapping(enriched, form, "potenciaModulos",       "e2_potenciaPicoModulo");
        applyMapping(enriched, form, "potenciaPicoGenerador", "e2_potenciaPicoGenerador");
        applyMapping(enriched, form, "disposicionModulos",   "orientacionGenerador");
        applyMapping(enriched, form, "tipoInstalacion",      "tipoInstalacionRecarga");

        // Variables de cabecera y descriptivas (Defaults de Vue)
        putIfAbsent(enriched, "pse_tipo",        "Instalación Solar Fotovoltaica");
        
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
        applyMapping(enriched, form, "pse_dia",  "dia");
        applyMapping(enriched, form, "pse_mes",  "mes");
        applyMapping(enriched, form, "pse_anio", "anio");
        
        // Fallbacks para fecha
        putIfAbsent(enriched, "pse_dia",  "___");
        putIfAbsent(enriched, "pse_mes",  "___");
        putIfAbsent(enriched, "pse_anio", "___");
    }

    // =========================================================================
    // MAPEO COMÚN (para plantillas sin configuración específica)
    // =========================================================================
    private void applyCommonFieldMapping(Map<String, Object> enriched, Map<String, Object> form) {
        applyMapping(enriched, form, "localidad",    "localidadEmplazamiento");
        applyMapping(enriched, form, "provincia",    "provinciaEmplazamiento");
        applyMapping(enriched, form, "codigoPostal", "codigoPostalEmplazamiento");
        applyMapping(enriched, form, "ciudadFirma",  "localidadEmplazamiento");
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
        if (!bloque.isEmpty()) sb.append(" Bloque ").append(bloque);
        String escalera = getString(form, "escalera");
        if (!escalera.isEmpty()) sb.append(" Escalera ").append(escalera);
        String planta = getString(form, "planta");
        if (!planta.isEmpty()) sb.append(" Planta ").append(planta);
        String puerta = getString(form, "puerta");
        if (!puerta.isEmpty()) sb.append(" Puerta ").append(puerta);
        return sb.toString().trim();
    }

    /**
     * Copia el valor de `sourceKey` del formulario BD a `targetKey` en el mapa enriquecido,
     * pero SÓLO si la fuente tiene valor. La clave destino siempre se sobreescribe con el
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
     * Sólo inserta el valor por defecto si la clave no existe o está vacía en el mapa.
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
            if (sb.length() > 0) sb.append(" ");
            sb.append(value);
        }
    }
}
