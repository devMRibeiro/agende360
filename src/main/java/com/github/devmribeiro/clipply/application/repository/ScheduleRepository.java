package com.github.devmribeiro.clipply.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

}