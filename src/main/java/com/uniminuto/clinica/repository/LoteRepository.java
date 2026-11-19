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
 * Repositorio Spring Data JPA de {@link LoteMedicamento}.
 */
@Repository
public interface LoteRepository extends JpaRepository<LoteMedicamento, Long> {

    /** @param medicamentoId medicamento. @return lotes del medicamento, el que vence primero al inicio. */
    List<LoteMedicamento> findAllByMedicamentoIdOrderByFechaVencimientoAsc(Long medicamentoId);

    /** @param medicamentoId medicamento. @param numeroLote número de lote. @return el lote, si existe. */
    Optional<LoteMedicamento> findByMedicamentoIdAndNumeroLote(Long medicamentoId, String numeroLote);

    /**
     * Lotes vigentes con existencias en orden FEFO (primero el que vence antes), bloqueados para escritura.
     * @param medicamentoId medicamento. @param hoy fecha actual. @return lotes disponibles.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteMedicamento l where l.medicamentoId = :m and l.cantidadDisponible > 0 and l.fechaVencimiento >= :hoy order by l.fechaVencimiento asc, l.id asc")
    List<LoteMedicamento> lotesFefoBloqueados(@Param("m") Long medicamentoId, @Param("hoy") LocalDate hoy);

    /** Lotes vigentes con existencias (sin bloqueo) en orden FEFO. @param m medicamento. @param hoy fecha. @return lotes. */
    @Query("select l from LoteMedicamento l where l.medicamentoId = :m and l.cantidadDisponible > 0 and l.fechaVencimiento >= :hoy order by l.fechaVencimiento asc, l.id asc")
    List<LoteMedicamento> lotesFefo(@Param("m") Long m, @Param("hoy") LocalDate hoy);

    /** Suma de existencias vigentes. @param m medicamento. @param hoy fecha. @return unidades vigentes. */
    @Query("select coalesce(sum(l.cantidadDisponible),0) from LoteMedicamento l where l.medicamentoId = :m and l.fechaVencimiento >= :hoy")
    Long stockVigente(@Param("m") Long m, @Param("hoy") LocalDate hoy);

    /** Bloquea un lote para escritura. @param id lote. @return lote bloqueado. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteMedicamento l where l.id = :id")
    Optional<LoteMedicamento> bloquear(@Param("id") Long id);
}
