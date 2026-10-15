package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Especializacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para la entidad {@link Especializacion}.
 */
@Repository
public interface EspecializacionRepository extends JpaRepository<Especializacion, Long> {
}
