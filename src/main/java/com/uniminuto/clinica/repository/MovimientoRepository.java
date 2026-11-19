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
 * Repositorio Spring Data JPA de {@link MovimientoInventario}.
 */
@Repository
public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Long> {

    /** @param medicamentoId medicamento. @return historial del medicamento, más reciente primero. */
    List<MovimientoInventario> findAllByMedicamentoIdOrderByFechaDescIdDesc(Long medicamentoId);

    /** @return todo el historial, más reciente primero. */
    List<MovimientoInventario> findAllByOrderByFechaDescIdDesc();

    /** @param despachoId despacho. @param tipo tipo. @return movimientos del despacho. */
    List<MovimientoInventario> findAllByDespachoIdAndTipo(Long despachoId, String tipo);

    /** @param despachoId despacho. @return todos los movimientos del despacho. */
    List<MovimientoInventario> findAllByDespachoId(Long despachoId);
}
