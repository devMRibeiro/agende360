package br.com.corestacks.agende360.outbox.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.outbox.model.OutboxEvent;
import br.com.corestacks.agende360.outbox.model.OutboxStatus;
import jakarta.transaction.Transactional;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
	
    List<OutboxEvent> findTop50ByEventStatusOrderByCreatedAtAsc(OutboxStatus status);
    
    @Modifying
    @Transactional
    @Query("delete from outbox_event oe where oe.eventStatus = :status and oe.createdAt < :limit")
    int deleteOldEvents(@Param("status") OutboxStatus status, @Param("limit") Instant limit);
}