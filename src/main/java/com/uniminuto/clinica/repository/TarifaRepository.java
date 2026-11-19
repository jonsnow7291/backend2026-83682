package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA de {@link Tarifa}.
 */
@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    /** @return tarifas activas. */
    List<Tarifa> findAllByActivaTrueOrderByEspecializacionIdAscServicioAsc();

    /** @param especializacionId especialidad. @return tarifas activas de la especialidad. */
    List<Tarifa> findAllByEspecializacionIdAndActivaTrueOrderByIdAsc(Long especializacionId);
}
