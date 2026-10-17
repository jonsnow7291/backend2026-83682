package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

/**
 * Servicio de negocio para la gestión de mascotas.
 */
public interface MascotaService {

    /**
     * Lista todas las mascotas registradas.
     *
     * @return lista de mascotas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    List<Mascota> listarMascotas() throws BadRequestException;

    /**
     * Lista todas las mascotas ordenadas por nombre.
     *
     * @param ascendente {@code true} para orden ascendente, {@code false} para descendente.
     * @return lista ordenada de mascotas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    List<Mascota> listarMascotasOrdenado(boolean ascendente) throws BadRequestException;

    /**
     * Lista las mascotas de una raza.
     *
     * @param idRaza identificador de la raza.
     * @return mascotas de la raza.
     * @throws BadRequestException si el identificador es inválido o la raza no existe.
     */
    List<Mascota> listarMascotasPorRaza(Integer idRaza) throws BadRequestException;

    /**
     * Lista las mascotas de un cliente.
     *
     * @param idCliente identificador del cliente.
     * @return mascotas del cliente.
     * @throws BadRequestException si el identificador es inválido o el cliente no existe.
     */
    List<Mascota> listarMascotasPorCliente(Long idCliente) throws BadRequestException;

    /**
     * Consulta una mascota específica por su identificador.
     *
     * @param mascotaId identificador de la mascota.
     * @return la mascota encontrada.
     * @throws BadRequestException si el identificador es inválido o la mascota no existe.
     */
    Mascota consultarMascota(Integer mascotaId) throws BadRequestException;

    /**
     * Registra una nueva mascota validando sus datos, la raza y el cliente.
     *
     * @param mascotaRq datos de la mascota.
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o la raza/cliente no existen.
     */
    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    /**
     * Actualiza una mascota existente validando sus datos, la raza y el cliente.
     *
     * @param mascotaRq datos actualizados de la mascota (incluye {@code mascotaId}).
     * @return respuesta con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o la mascota, raza o cliente no existen.
     */
    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

}
