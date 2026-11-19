package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.AtencionMedica;
import com.uniminuto.clinica.models.AtencionRq;

import java.util.List;

/**
 * Atención médica a partir de una cita: apertura, registro clínico, finalización y prefactura.
 */
public interface AtencionService {

    /**
     * Abre la atención de una cita (la cita pasa a ATENDIDA).
     *
     * @param rq cita y datos iniciales.
     * @return atención abierta.
     */
    AtencionMedica abrir(AtencionRq rq);

    /**
     * Registra síntomas, diagnóstico, observaciones y recomendaciones mientras está EN_CURSO.
     *
     * @param rq datos con id.
     * @return atención actualizada.
     */
    AtencionMedica registrar(AtencionRq rq);

    /**
     * Finaliza la atención: la cita pasa a FINALIZADA y queda en el historial clínico de la mascota.
     *
     * @param id atención.
     * @return atención finalizada.
     */
    AtencionMedica finalizar(Long id);

    /**
     * @param id atención.
     * @return la atención.
     */
    AtencionMedica consultar(Long id);

    /**
     * @param mascotaId mascota.
     * @return atenciones de la mascota, más reciente primero.
     */
    List<AtencionMedica> historialMascota(Integer mascotaId);

    /**
     * Genera la prefactura (servicios + medicamentos formulados, valores estimados).
     *
     * @param id atención.
     * @return bytes del PDF.
     */
    byte[] prefacturaPdf(Long id);
}
