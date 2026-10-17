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

    /**
     * Indica si una historia médica tiene anotaciones registradas. Utilizado para
     * impedir la eliminación de historias con información clínica asociada.
     *
     * @param historiaId identificador de la historia médica.
     * @return {@code true} si la historia tiene al menos una anotación.
     */
    boolean existsByHistoriaMedica_Id(Long historiaId);
}
