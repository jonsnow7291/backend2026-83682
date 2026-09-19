package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Medicamento}.
 */
@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
}
