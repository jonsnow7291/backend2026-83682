package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Medico}.
 * Se usa únicamente para validar la existencia de un médico referenciado
 * desde citas y anotaciones de historia médica.
 */
@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
}
