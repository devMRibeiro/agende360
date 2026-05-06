package br.com.corestacks.agende360.application.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.model.Schedule;
import br.com.corestacks.agende360.application.type.DayOfWeek;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findByCompanyId(UUID companyId);

    List<Schedule> findByCompanyIdAndDayOfWeek(UUID companyId, DayOfWeek dayOfWeek);
    
    // Verifica se existe sobreposição de horário no mesmo dia para a empresa.
    // Exclui o próprio registro (usado na edição) via excludeId.
    // Sobreposição ocorre quando o novo intervalo (newStart, newEnd) intercepta algum existente.
    @Query("SELECT s FROM Schedule s " +
           "WHERE s.companyId = :companyId " +
           "AND s.dayOfWeek = :dayOfWeek " +
           "AND s.id <> :excludeId " +
           "AND s.startTime < :newEnd " +
           "AND s.endTime > :newStart")
    List<Schedule> findOverlapping(UUID companyId, DayOfWeek dayOfWeek, LocalTime newStart, LocalTime newEnd, UUID excludeId);
}