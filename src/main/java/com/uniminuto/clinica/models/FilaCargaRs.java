package com.uniminuto.clinica.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Resultado de validar una fila del archivo de carga masiva.
 */
@Data
@AllArgsConstructor
public class FilaCargaRs {

    /** Número de fila en el archivo (los datos empiezan en la 2). */
    private int fila;
    /** Código de la fila. */
    private String codigo;
    /** Nombre de la fila. */
    private String nombre;
    /** {@code true} si la fila es válida. */
    private boolean valida;
    /** Motivos de rechazo (vacío si es válida). */
    private List<String> errores;
}
