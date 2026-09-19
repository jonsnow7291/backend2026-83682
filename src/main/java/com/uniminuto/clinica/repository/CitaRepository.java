package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Cita}.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Busca las citas cuya fecha y hora se encuentren dentro del rango indicado,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial límite inferior del rango (inclusive).
     * @param fechaFinal   límite superior del rango (inclusive).
     * @return lista de citas dentro del rango, ordenada descendentemente por fecha y hora.
     */
    List<Cita> findAllByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);
}
