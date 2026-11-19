package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Medicamento}.
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    /** @param codigo código de 8 caracteres. @return producto con ese código. */
    java.util.Optional<Medicamento> findByCodigoIgnoreCase(String codigo);

    /** @param nombre nombre. @return {@code true} si existe un producto con ese nombre. */
    boolean existsByNombreIgnoreCase(String nombre);

    /** @param codigo código. @return {@code true} si existe. */
    boolean existsByCodigoIgnoreCase(String codigo);

    /** @param tipo tipo opcional. @param q texto opcional. @return productos filtrados por tipo y nombre/código. */
    @org.springframework.data.jpa.repository.Query("select m from Medicamento m where (:tipo is null or m.tipo = :tipo) and (:q is null or lower(m.nombre) like lower(concat('%', :q, '%')) or lower(m.codigo) like lower(concat('%', :q, '%'))) order by m.nombre")
    java.util.List<Medicamento> buscar(@org.springframework.data.repository.query.Param("tipo") String tipo, @org.springframework.data.repository.query.Param("q") String q);

    /** @return productos activos sin existencias vigentes. */
    java.util.List<Medicamento> findAllByActivoTrueAndStockActualLessThanEqualOrderByNombre(Integer stock);

    /** @return activos con stock bajo (entre 1 y el mínimo). */
    @org.springframework.data.jpa.repository.Query("select m from Medicamento m where m.activo = true and m.stockActual > 0 and m.stockActual <= m.stockMinimo order by m.nombre")
    java.util.List<Medicamento> stockBajo();

    /** @param hoy fecha. @param limite fecha límite. @return productos con lotes con existencias que vencen en [hoy, limite]. */
    @org.springframework.data.jpa.repository.Query("select m from Medicamento m where exists (select 1 from LoteMedicamento l where l.medicamentoId = m.id and l.cantidadDisponible > 0 and l.fechaVencimiento between :hoy and :lim) order by m.nombre")
    java.util.List<Medicamento> proximosAVencer(@org.springframework.data.repository.query.Param("hoy") java.time.LocalDate hoy, @org.springframework.data.repository.query.Param("lim") java.time.LocalDate limite);

    /** @param hoy fecha. @return productos con lotes vencidos que aún tienen unidades. */
    @org.springframework.data.jpa.repository.Query("select m from Medicamento m where exists (select 1 from LoteMedicamento l where l.medicamentoId = m.id and l.cantidadDisponible > 0 and l.fechaVencimiento < :hoy) order by m.nombre")
    java.util.List<Medicamento> conLotesVencidos(@org.springframework.data.repository.query.Param("hoy") java.time.LocalDate hoy);
}
