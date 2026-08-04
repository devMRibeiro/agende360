package br.com.corestacks.agende360.outbox.publisher;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.outbox.handler.OutboxEventHandler;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;
import br.com.corestacks.agende360.outbox.model.OutboxStatus;
import br.com.corestacks.agende360.outbox.repository.OutboxEventRepository;

@Service
public class OutboxPublisherService {

	private final List<OutboxEventHandler> handlers;
	private final OutboxEventRepository outboxEventRepository;

	public OutboxPublisherService(
			List<OutboxEventHandler> handlers,
			OutboxEventRepository outboxEventRepository) {
		this.outboxEventRepository = outboxEventRepository;
		this.handlers = handlers;
	}

	public void publish(OutboxEvent event) {
    	OutboxEventHandler found = null;

    	for (OutboxEventHandler handler : handlers) {
    	    if (handler.supports(event.getEventType())) {
    	        found = handler;
    	        break;
    	    }
    	}

    	if (found == null)
    	    throw new IllegalStateException("Nenhum handler encontrado para " + event.getEventType());

    	try {
    		
    		found.handle(event);
    		
    		event.setEventStatus(OutboxStatus.PROCESSED);
    	    event.setSentAt(Instant.now());
    		
    	} catch (Exception e) {
            int retry = event.getRetryCount() + 1;
            event.setRetryCount(retry);
            event.setNextAttemptAt(Instant.now().plusSeconds(calculateBackoff(retry)));
        }

    	outboxEventRepository.save(event);
    }
	
	private long calculateBackoff(int retryCount) {
	    return Math.min((long) Math.pow(2, retryCount) * 30, 3600);
	}
}