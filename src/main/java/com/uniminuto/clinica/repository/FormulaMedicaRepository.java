package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link FormulaMedica}.
 */
@Repository
public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {

    /**
     * Lista todas las fórmulas médicas ordenadas de la más reciente a la más antigua.
     *
     * @return lista completa de fórmulas médicas ordenada descendentemente por fecha de creación.
     */
    List<FormulaMedica> findAllByOrderByFechaCreacionRegistroDesc();
}
