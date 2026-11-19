package com.uniminuto.clinica.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Resumen de una carga masiva: previsualización (sin guardar) o resultado de la confirmación.
 */
@Data
public class CargaMasivaRs {

    /** Id de la carga registrada (solo al confirmar). */
    private Long cargaId;
    /** Nombre del archivo. */
    private String archivo;
    /** Total de filas de datos. */
    private int totalFilas;
    /** Filas válidas (o importadas). */
    private int validas;
    /** Filas rechazadas. */
    private int rechazadas;
    /** Detalle por fila. */
    private List<FilaCargaRs> filas = new ArrayList<>();
}
