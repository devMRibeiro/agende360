package br.com.corestacks.agende360.outbox.publisher;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.outbox.handler.OutboxEventHandler;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Service
public class OutboxPublisherService {

	private final List<OutboxEventHandler> handlers;

	public OutboxPublisherService(List<OutboxEventHandler> handlers) {
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

    	found.handle(event);
    }
}