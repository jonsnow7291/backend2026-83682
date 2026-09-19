package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.AnotacionHistoria;
import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link AnotacionHistoria}.
 */
@Repository
public interface AnotacionHistoriaRepository extends JpaRepository<AnotacionHistoria, Long> {

    /**
     * Lista las anotaciones asociadas a una historia médica, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param historiaMedica historia médica a la que pertenecen las anotaciones.
     * @return lista de anotaciones de la historia médica indicada.
     */
    List<AnotacionHistoria> findAllByHistoriaMedicaOrderByFechaDesc(HistoriaMedica historiaMedica);
}
