package br.com.corestacks.agende360.outbox.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.corestacks.agende360.outbox.model.OutboxEvent;
import br.com.corestacks.agende360.outbox.publisher.OutboxPublisherService;
import br.com.corestacks.agende360.outbox.service.OutboxEventService;

@Component
public class OutboxEventScheduler {

	private final static Logger LOGGER = LoggerFactory.getLogger(OutboxEventScheduler.class);
	
	private final OutboxEventService outboxEventService;
	private final OutboxPublisherService outboxPublisherService;
	
	public OutboxEventScheduler(OutboxEventService outboxEventService, OutboxPublisherService outboxPublisherService) {
		this.outboxEventService = outboxEventService;
		this.outboxPublisherService = outboxPublisherService;
	}

	@Scheduled(cron = "*/30 * * * * *") // every 30s
	public void execute() {

	    List<OutboxEvent> events = outboxEventService.findPending();
	    
	    for (OutboxEvent event : events) {
			try {

				LOGGER.info("Processando evento -> {}", event.getEventType());
				outboxPublisherService.publish(event);
				outboxEventService.markAsProcessed(event);

			} catch (Exception e) {
				int retry = event.getRetryCount() + 1;

				event.setRetryCount(retry);
				event.setLastError(e.getMessage());

				if (retry >= event.getEventType().getMaxRetries()) {
				    outboxEventService.markAsFailed(event, e);
				} else {
				    outboxEventService.markAsPending(event);
				}
				
				LOGGER.info("Erro ao processar o evento -> {}, Tentativa -> {}", event.getEventType(), retry);
			} 
	    }
	}
}