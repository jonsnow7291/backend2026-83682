package com.uniminuto.clinica.util;

import java.util.List;

/**
 * Estados válidos de una cita y utilidades para compararlos.
 */
public final class CitaEstados {

    /** Cita agendada, sin confirmar. */
    public static final String PENDIENTE = "PENDIENTE";
    /** Cita confirmada con el propietario. */
    public static final String CONFIRMADA = "CONFIRMADA";
    /** Cita atendida (atención médica abierta o realizada). */
    public static final String ATENDIDA = "ATENDIDA";
    /** Cita cancelada (se conserva el historial). */
    public static final String CANCELADA = "CANCELADA";
    /** Cita finalizada (atención cerrada). */
    public static final String FINALIZADA = "FINALIZADA";

    /** Todos los estados permitidos. */
    public static final List<String> TODOS = List.of(PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, FINALIZADA);

    private CitaEstados() {
    }

    /**
     * @param estado estado de una cita.
     * @return {@code true} si la cita ya fue atendida (ATENDIDA o FINALIZADA).
     */
    public static boolean fueAtendida(String estado) {
        return ATENDIDA.equalsIgnoreCase(estado) || FINALIZADA.equalsIgnoreCase(estado);
    }
}
