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
 * Repositorio Spring Data JPA de {@link CargaMasivaDetalle}.
 */
@Repository
public interface CargaMasivaDetalleRepository extends JpaRepository<CargaMasivaDetalle, Long> {

    /** @param cargaId carga. @return filas de la carga. */
    List<CargaMasivaDetalle> findAllByCargaIdOrderByFila(Long cargaId);
}
