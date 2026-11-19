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
 * Repositorio Spring Data JPA de {@link Despacho}.
 */
@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {

    /** @param formulaId fórmula. @return despacho de la fórmula. */
    Optional<Despacho> findByFormulaId(Long formulaId);

    /** @param estados estados. @return despachos en esos estados, más antiguos primero. */
    List<Despacho> findAllByEstadoInOrderByFechaCreacionAsc(List<String> estados);

    /** @return historial completo. */
    List<Despacho> findAllByOrderByFechaCreacionDesc();

    /** Bloquea el despacho para escritura (evita doble descuento concurrente). @param id despacho. @return despacho bloqueado. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Despacho d where d.id = :id")
    Optional<Despacho> bloquear(@Param("id") Long id);
}
