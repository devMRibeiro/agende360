package br.com.corestacks.agende360.outbox.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.outbox.model.OutboxEvent;
import br.com.corestacks.agende360.outbox.model.OutboxStatus;
import br.com.corestacks.agende360.outbox.repository.OutboxEventRepository;

@Service
public class OutboxEventService {

	private final OutboxEventRepository outboxEventRepository;

	public OutboxEventService(OutboxEventRepository outboxEventRepository) {
		this.outboxEventRepository = outboxEventRepository;
	}
	
	public void saveAll(List<OutboxEvent> events) {
		outboxEventRepository.saveAll(events);
    }

    public List<OutboxEvent> findPending() {
        return outboxEventRepository.findPendingEvents(OutboxStatus.PENDING, LocalDateTime.now(), PageRequest.of(0, 50));
    }

    public void markAsProcessed(OutboxEvent event) {
        event.setEventStatus(OutboxStatus.PROCESSED);
        event.setLastError(null);
        event.setSentAt(Instant.now());
        outboxEventRepository.save(event);
    }

    public void markAsFailed(OutboxEvent event, Exception e) {
        event.setEventStatus(OutboxStatus.ERROR);
        event.setLastError(e.getMessage());
        outboxEventRepository.save(event);
    }

    public void markAsPending(OutboxEvent event) {
    	event.setEventStatus(OutboxStatus.PENDING);
    	event.calculeNextAttempt();
    	outboxEventRepository.save(event);
    }
    
    public void deleteOldEvents() {
    	outboxEventRepository.deleteOldEvents(OutboxStatus.PROCESSED, LocalDateTime.now().minus(3, ChronoUnit.DAYS));
    }
}