package com.uniminuto.clinica.repository;

import com.uniminuto.clinica.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para la entidad {@link Cita}.
 * Proporciona métodos de consulta derivados para la gestión de citas y reglas de negocio.
 */
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    /**
     * Obtiene todas las citas ordenadas descendentemente por fecha y hora.
     *
     * @return lista completa de citas ordenadas de la más reciente a la más antigua.
     */
    List<Cita> findAllByOrderByFechaHoraDesc();

    /**
     * Busca las citas cuya fecha y hora se encuentren dentro del rango indicado,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param fechaInicial límite inferior del rango (inclusive).
     * @param fechaFinal   límite superior del rango (inclusive).
     * @return lista de citas dentro del rango, ordenada descendentemente por fecha y hora.
     */
    List<Cita> findAllByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime fechaInicial, LocalDateTime fechaFinal);

    /**
     * Busca citas de un médico veterinario específico en un rango de fechas/horas determinado.
     * Utilizado para validar la regla de negocio de no solapamiento (+/- 15 minutos).
     *
     * @param medicoId    identificador del médico veterinario.
     * @param fechaInicio límite inferior del intervalo (fechaHora - 15 minutos).
     * @param fechaFin    límite superior del intervalo (fechaHora + 15 minutos).
     * @return lista de citas asignadas al médico en ese intervalo de tiempo.
     */
    List<Cita> findAllByMedico_IdAndFechaHoraBetween(Long medicoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Busca citas agendadas para una mascota específica en una fecha y hora exacta.
     * Utilizado para validar la regla de negocio de no citas simultáneas para la misma mascota.
     *
     * @param mascotaId identificador de la mascota.
     * @param fechaHora fecha y hora exacta de la cita.
     * @return lista de citas de la mascota en dicha fecha y hora.
     */
    List<Cita> findAllByMascota_MascotaIdAndFechaHora(Integer mascotaId, LocalDateTime fechaHora);

    /**
     * Busca las citas de una mascota con el estado indicado (sin distinguir mayúsculas),
     * ordenadas de la más reciente a la más antigua. Utilizado para ofrecer las citas
     * atendidas sobre las que se puede emitir una fórmula médica.
     *
     * @param mascotaId identificador de la mascota.
     * @param estado    estado de la cita (ej. {@code atendida}).
     * @return lista de citas de la mascota en el estado indicado.
     */
    List<Cita> findAllByMascota_MascotaIdAndEstadoIgnoreCaseOrderByFechaHoraDesc(Integer mascotaId, String estado);

    /**
     * Citas activas (no canceladas) del médico que se solapan con el intervalo dado.
     *
     * @param medicoId médico. @param inicio inicio. @param fin fin. @param excluir id a excluir (o -1).
     * @return citas en conflicto.
     */
    @org.springframework.data.jpa.repository.Query("select c from Cita c where c.medico.id = :m and c.estado <> 'CANCELADA' and c.fechaHora < :fin and c.fechaHoraFin > :ini and c.id <> :ex")
    List<Cita> solapadasMedico(@org.springframework.data.repository.query.Param("m") Long medicoId,
                               @org.springframework.data.repository.query.Param("ini") LocalDateTime inicio,
                               @org.springframework.data.repository.query.Param("fin") LocalDateTime fin,
                               @org.springframework.data.repository.query.Param("ex") Long excluir);

    /**
     * Citas activas (no canceladas) de la mascota que se solapan con el intervalo dado.
     *
     * @param mascotaId mascota. @param inicio inicio. @param fin fin. @param excluir id a excluir (o -1).
     * @return citas en conflicto.
     */
    @org.springframework.data.jpa.repository.Query("select c from Cita c where c.mascota.mascotaId = :m and c.estado <> 'CANCELADA' and c.fechaHora < :fin and c.fechaHoraFin > :ini and c.id <> :ex")
    List<Cita> solapadasMascota(@org.springframework.data.repository.query.Param("m") Integer mascotaId,
                                @org.springframework.data.repository.query.Param("ini") LocalDateTime inicio,
                                @org.springframework.data.repository.query.Param("fin") LocalDateTime fin,
                                @org.springframework.data.repository.query.Param("ex") Long excluir);

    /**
     * Citas en un rango, opcionalmente de un solo médico.
     *
     * @param medicoId médico o {@code null} para todos. @param desde inicio. @param hasta fin.
     * @return citas ordenadas por fecha.
     */
    @org.springframework.data.jpa.repository.Query("select c from Cita c where (:m is null or c.medico.id = :m) and c.fechaHora >= :d and c.fechaHora < :h order by c.fechaHora")
    List<Cita> enRango(@org.springframework.data.repository.query.Param("m") Long medicoId,
                       @org.springframework.data.repository.query.Param("d") LocalDateTime desde,
                       @org.springframework.data.repository.query.Param("h") LocalDateTime hasta);

    /**
     * Historial completo de citas de una mascota.
     *
     * @param mascotaId mascota.
     * @return citas, la más reciente primero.
     */
    List<Cita> findAllByMascota_MascotaIdOrderByFechaHoraDesc(Integer mascotaId);

    /**
     * Bloquea la cita para escritura (serializa operaciones concurrentes sobre ella).
     *
     * @param id cita. @return cita bloqueada.
     */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from Cita c where c.id = :id")
    java.util.Optional<Cita> bloquear(@org.springframework.data.repository.query.Param("id") Long id);
}
