package com.uniminuto.clinica.security;

/**
 * Constantes con los roles del sistema. El valor guardado en {@code usuario.rol} coincide con
 * estos nombres; Spring Security les antepone el prefijo {@code ROLE_}.
 */
public final class Roles {

    /** Administrador: acceso total, tarifas, usuarios y reportes. */
    public static final String ADMIN = "ADMIN";

    /** Secretaria: agenda de citas, clientes, mascotas e inventario. */
    public static final String SECRETARIA = "SECRETARIA";

    /** Médico veterinario: sus citas, atención clínica y fórmulas. */
    public static final String MEDICO = "MEDICO";

    /** Despacho médico: solo consulta de inventario y módulo de despacho. */
    public static final String DESPACHO_MEDICO = "DESPACHO_MEDICO";

    private Roles() {
    }
}
