package com.uniminuto.clinica.service;

import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;

import java.util.List;

public interface MascotaService {

    List<Mascota> listarMascotas() throws BadRequestException;

    List<Mascota> listarMascotasOrdenado(boolean ascendente) throws BadRequestException;

    List<Mascota> listarMascotasPorRaza(Integer idRaza) throws BadRequestException;

    List<Mascota> listarMascotasPorCliente(Long idCliente) throws BadRequestException;

    /**
     * Consulta una mascota específica por su identificador.
     *
     * @param mascotaId identificador de la mascota.
     * @return la mascota encontrada.
     * @throws BadRequestException si el identificador es inválido o la mascota no existe.
     */
    Mascota consultarMascota(Integer mascotaId) throws BadRequestException;

    MiRespuestaRS guardarMascota(MascotaRq mascotaRq) throws BadRequestException;

    MiRespuestaRS actualizarMascota(MascotaRq mascotaRq) throws BadRequestException;

}
