package br.com.corestacks.agende360.outbox.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.outbox.model.OutboxEvent;
import br.com.corestacks.agende360.outbox.model.OutboxStatus;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop50ByEventStatusOrderByCreatedAtAsc(OutboxStatus status);
}