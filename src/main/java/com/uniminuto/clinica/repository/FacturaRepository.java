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
 * Repositorio Spring Data JPA de {@link Factura}.
 */
@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    /** @param citaId cita. @param estado estado a excluir. @return factura no anulada de la cita. */
    Optional<Factura> findFirstByCitaIdAndEstadoPagoNot(Long citaId, String estado);

    /** @param clienteId propietario. @return facturas del propietario. */
    List<Factura> findAllByClienteIdOrderByFechaDesc(Long clienteId);

    /** @param mascotaId mascota. @return facturas de la mascota. */
    List<Factura> findAllByMascotaIdOrderByFechaDesc(Integer mascotaId);

    /** @return todas las facturas. */
    List<Factura> findAllByOrderByFechaDesc();

    /** Bloquea la factura para escritura. @param id factura. @return factura bloqueada. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Factura f where f.id = :id")
    Optional<Factura> bloquear(@Param("id") Long id);
}
