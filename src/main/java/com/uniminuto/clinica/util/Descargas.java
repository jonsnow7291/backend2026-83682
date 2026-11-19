package com.uniminuto.clinica.util;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Fábrica de respuestas HTTP para descargar archivos generados (PDF, Excel, CSV).
 */
public final class Descargas {

    /** Tipo MIME de archivos .xlsx. */
    public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private Descargas() {
    }

    /**
     * @param nombre nombre del archivo.
     * @param datos  bytes del PDF.
     * @return respuesta con el PDF como adjunto.
     */
    public static ResponseEntity<byte[]> pdf(String nombre, byte[] datos) {
        return respuesta(nombre, datos, MediaType.APPLICATION_PDF);
    }

    /**
     * @param nombre nombre del archivo.
     * @param datos  bytes del Excel.
     * @return respuesta con el .xlsx como adjunto.
     */
    public static ResponseEntity<byte[]> xlsx(String nombre, byte[] datos) {
        return respuesta(nombre, datos, MediaType.parseMediaType(XLSX));
    }

    private static ResponseEntity<byte[]> respuesta(String nombre, byte[] datos, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .contentType(tipo)
                .body(datos);
    }
}
