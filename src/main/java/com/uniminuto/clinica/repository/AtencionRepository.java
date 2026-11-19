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
 * Repositorio Spring Data JPA de {@link AtencionMedica}.
 */
@Repository
public interface AtencionRepository extends JpaRepository<AtencionMedica, Long> {

    /** @param citaId cita. @return la atención de la cita. */
    Optional<AtencionMedica> findByCitaId(Long citaId);

    /** @param estado estado. @return atenciones en ese estado. */
    List<AtencionMedica> findAllByOrderByFechaInicioDesc();
}
