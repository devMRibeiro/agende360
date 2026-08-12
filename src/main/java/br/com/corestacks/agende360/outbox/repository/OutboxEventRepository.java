package br.com.corestacks.agende360.outbox.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
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
	
	@Query("select e from OutboxEvent e where e.eventStatus = :status and (e.nextAttemptAt is null or e.nextAttemptAt <= :nextAttemptAt) order by e.createdAt asc")
	List<OutboxEvent> findPendingEvents(@Param("status") OutboxStatus status, @Param("nextAttemptAt") LocalDateTime nextAttemptAt, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query("delete from OutboxEvent e where e.eventStatus = :status and e.createdAt < :limit")
	int deleteOldEvents(@Param("status") OutboxStatus status, @Param("limit") LocalDateTime limit);
}