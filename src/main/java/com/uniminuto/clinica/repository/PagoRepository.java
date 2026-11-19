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
 * Repositorio Spring Data JPA de {@link Pago}.
 */
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** @param facturaId factura. @return pagos de la factura. */
    List<Pago> findAllByFacturaIdOrderByFecha(Long facturaId);

    /** @param facturaId factura. @return total pagado. */
    @Query("select coalesce(sum(p.valor),0) from Pago p where p.facturaId = :f")
    java.math.BigDecimal totalPagado(@Param("f") Long facturaId);
}
