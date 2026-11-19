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
 * Repositorio Spring Data JPA de {@link HorarioVeterinario}.
 */
@Repository
public interface HorarioRepository extends JpaRepository<HorarioVeterinario, Long> {

    /** @param medicoId médico. @return horarios del médico. */
    List<HorarioVeterinario> findAllByMedicoIdOrderByDiaSemanaAscHoraInicioAsc(Long medicoId);

    /** @param medicoId médico. @param dia día ISO. @return franjas activas de ese día. */
    List<HorarioVeterinario> findAllByMedicoIdAndDiaSemanaAndActivoTrue(Long medicoId, Integer dia);
}
