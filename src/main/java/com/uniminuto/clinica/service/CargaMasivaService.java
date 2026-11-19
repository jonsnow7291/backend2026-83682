package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.CargaMasiva;
import com.uniminuto.clinica.models.CargaMasivaRs;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Carga masiva de inventario desde archivos .xlsx o .csv, en dos pasos: previsualizar y confirmar.
 */
public interface CargaMasivaService {

    /**
     * Valida el archivo y devuelve filas correctas y con error. No guarda nada.
     *
     * @param archivo archivo .xlsx o .csv.
     * @return resultado de la validación.
     */
    CargaMasivaRs previsualizar(MultipartFile archivo);

    /**
     * Importa solo las filas válidas (cada una en su propia transacción) y registra el log.
     *
     * @param archivo archivo .xlsx o .csv.
     * @return resultado de la importación con el id de la carga.
     */
    CargaMasivaRs confirmar(MultipartFile archivo);

    /**
     * @return historial de cargas, más reciente primero.
     */
    List<CargaMasiva> historial();

    /**
     * Genera el informe de errores (filas rechazadas) de una carga.
     *
     * @param cargaId carga.
     * @return bytes de un .xlsx.
     */
    byte[] informeErrores(Long cargaId);

    /**
     * Genera la plantilla de ejemplo del archivo de carga.
     *
     * @param formato {@code xlsx} o {@code csv}.
     * @return bytes de la plantilla.
     */
    byte[] plantilla(String formato);
}
