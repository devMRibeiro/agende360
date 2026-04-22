package com.github.devmribeiro.clipply.application.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Appointment;
import com.github.devmribeiro.clipply.application.type.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a WHERE a.companyId = :companyId AND a.status != 'CANCELLED'")
    List<Appointment> findActiveByCompanyId(UUID companyId);

    @Query("SELECT a FROM Appointment a WHERE a.companyId = :companyId " +
           "AND a.startTime >= :startOfDay AND a.startTime < :endOfDay " +
           "AND a.status != 'CANCELLED'")
    List<Appointment> findActiveByCompanyIdAndDate(UUID companyId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId " +
           "AND a.startTime >= :startOfDay AND a.startTime < :endOfDay " +
           "AND a.status != 'CANCELLED'")
    List<Appointment> findActiveByUserIdAndDate(UUID userId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query("SELECT a FROM Appointment a WHERE a.userId = :userId " +
           "AND a.startTime >= :startTime AND a.endTime <= :endTime " +
           "AND a.status != 'CANCELLED'")
    List<Appointment> findConflicts(UUID userId, LocalDateTime startTime, LocalDateTime endTime);

    Appointment findByToken(String token);
    
    @Query("SELECT a FROM Appointment a WHERE a.userId = :professionalId " +
    	   "AND CAST(a.startTime AS date) = :date " + 
    	   "AND a.status != 'CANCELLED'")
	List<Appointment> findByProfessionalIdAndDate(@Param("professionalId") UUID professionalId, @Param("date") LocalDate date);

    @Modifying
    @Query("UPDATE Appointment a SET a.status = :newStatus WHERE a.endTime < :now AND a.status = :currentStatus")
    int updateStatus(@Param("now") LocalDateTime now, @Param("currentStatus") AppointmentStatus currentStatus, @Param("newStatus") AppointmentStatus newStatus);
}