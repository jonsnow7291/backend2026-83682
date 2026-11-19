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
 * Repositorio Spring Data JPA de {@link TarifaLog}.
 */
@Repository
public interface TarifaLogRepository extends JpaRepository<TarifaLog, Long> {

    /** @param tarifaId tarifa. @return historial de la tarifa. */
    List<TarifaLog> findAllByTarifaIdOrderByFechaDesc(Long tarifaId);
}
