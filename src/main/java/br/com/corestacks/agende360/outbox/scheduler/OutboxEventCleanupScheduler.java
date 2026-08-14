package br.com.corestacks.agende360.outbox.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.corestacks.agende360.outbox.service.OutboxEventService;

@Component
public class OutboxEventCleanupScheduler {

	private final OutboxEventService outboxEventService;
	
	public OutboxEventCleanupScheduler(OutboxEventService outboxEventService) {
		this.outboxEventService = outboxEventService;
	}

	@Scheduled(cron = "0 0 * * */2 *") // every one hour
	public void run() {
		outboxEventService.deleteOldEvents();
	}
}