package com.uniminuto.clinica.api;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.models.MascotaRq;
import com.uniminuto.clinica.models.MiRespuestaRS;
import com.uniminuto.clinica.exception.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Contrato REST para la gestión de mascotas.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/mascota")
public interface MascotaApi {

    /**
     * Lista todas las mascotas registradas.
     *
     * @return respuesta HTTP con la lista de mascotas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar-mascotas",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas()
            throws BadRequestException;


    /**
     * Lista todas las mascotas ordenadas por nombre.
     *
     * @param ascendente {@code true} para orden ascendente, {@code false} para descendente.
     * @return respuesta HTTP con la lista ordenada de mascotas.
     * @throws BadRequestException si ocurre un error al procesar la solicitud.
     */
    @GetMapping(value = "/listar-mascotas-ordenado",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> listarMascotas(
            @RequestParam boolean ascendente)
            throws BadRequestException;

    /**
     * Lista las mascotas de una raza.
     *
     * @param razaId identificador de la raza.
     * @return respuesta HTTP con las mascotas de la raza.
     * @throws BadRequestException si el identificador es inválido o la raza no existe.
     */
    @GetMapping(value = "/buscar-by-raza",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarPorRaza(
            @RequestParam Integer razaId)
            throws BadRequestException;

    /**
     * Lista las mascotas de un cliente (combo dependiente Cliente -> Mascota).
     *
     * @param clienteId identificador del cliente.
     * @return respuesta HTTP con las mascotas del cliente.
     * @throws BadRequestException si el identificador es inválido o el cliente no existe.
     */
    @GetMapping(value = "/buscar-by-cliente",
            produces = {"application/json"})
    ResponseEntity<List<Mascota>> buscarPorCliente(
            @RequestParam Long clienteId)
            throws BadRequestException;


    /**
     * Consulta una mascota específica por su identificador.
     *
     * @param mascotaId identificador de la mascota.
     * @return respuesta HTTP con la mascota encontrada.
     * @throws BadRequestException si el identificador es inválido o la mascota no existe.
     */
    @GetMapping(value = "/consultar",
            produces = {"application/json"})
    ResponseEntity<Mascota> consultarMascota(
            @RequestParam Integer mascotaId)
            throws BadRequestException;

    /**
     * Registra una nueva mascota.
     *
     * @param mascotaRq datos de la mascota.
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o la raza/cliente no existen.
     */
    @PostMapping(value = "/guardar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> guardarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

    /**
     * Actualiza una mascota existente.
     *
     * @param mascotaRq datos actualizados de la mascota (incluye {@code mascotaId}).
     * @return respuesta HTTP con el resultado de la operación.
     * @throws BadRequestException si los datos son inválidos o la mascota, raza o cliente no existen.
     */
    @PutMapping(value = "/actualizar",
            produces = {"application/json"},
            consumes = {"application/json"})
    ResponseEntity<MiRespuestaRS> actualizarMascota(
            @RequestBody MascotaRq mascotaRq)
            throws BadRequestException;

}
