package br.com.corestacks.agende360.outbox.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        return outboxEventRepository.findTop50ByEventStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
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
        event.calculeNextAttempt();
        outboxEventRepository.save(event);
    }

    public void markAsPending(OutboxEvent event) {
    	event.setEventStatus(OutboxStatus.PENDING);
    	outboxEventRepository.save(event);
    }
    
    public void deleteOldEvents() {
    	outboxEventRepository.deleteOldEvents(OutboxStatus.PROCESSED, Instant.now().minus(3, ChronoUnit.DAYS));
    }
}