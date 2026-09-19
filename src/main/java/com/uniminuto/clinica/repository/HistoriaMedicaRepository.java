package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.HistoriaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link HistoriaMedica}.
 */
@Repository
public interface HistoriaMedicaRepository extends JpaRepository<HistoriaMedica, Long> {

    /**
     * Lista todas las historias médicas ordenadas de la más reciente a la más antigua.
     *
     * @return lista completa de historias médicas ordenada descendentemente por fecha de creación.
     */
    List<HistoriaMedica> findAllByOrderByFechaCreacionDesc();

    /**
     * Busca las historias médicas creadas dentro del rango de fechas indicado,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial límite inferior del rango (inclusive).
     * @param fechaFinal   límite superior del rango (inclusive).
     * @return lista de historias médicas dentro del rango, ordenada descendentemente por fecha de creación.
     */
    List<HistoriaMedica> findAllByFechaCreacionBetweenOrderByFechaCreacionDesc(
            LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
