package com.github.devmribeiro.clipply.application.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Schedule;
import com.github.devmribeiro.clipply.application.type.DayOfWeek;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findByCompanyId(UUID companyId);

    boolean existsByCompanyIdAndDayOfWeek(UUID companyId, DayOfWeek dayOfWeek);
}