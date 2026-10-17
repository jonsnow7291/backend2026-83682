package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.entity.Mascota;
import com.uniminuto.clinica.entity.Raza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Mascota}.
 */
@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Integer> {

    /**
     * Lista las mascotas de una raza.
     *
     * @param raza raza a consultar.
     * @return mascotas de la raza indicada.
     */
    List<Mascota> findAllByRaza(Raza raza);

    /**
     * Lista las mascotas de un cliente (combo dependiente Cliente -> Mascota).
     *
     * @param cliente cliente propietario.
     * @return mascotas del cliente indicado.
     */
    List<Mascota> findAllByCliente(Cliente cliente);
}
