package br.com.corestacks.agende360.outbox.publisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.handler.OutboxEventHandler;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Service
public class OutboxPublisherService {

    private final Map<OutboxEventType, OutboxEventHandler> mapHandlers;

    public OutboxPublisherService(List<OutboxEventHandler> handlers) {

    	Map<OutboxEventType, OutboxEventHandler> map = new HashMap<OutboxEventType, OutboxEventHandler>();

    	for (OutboxEventHandler handler : handlers)
    	    map.put(handler.supports(), handler);

    	this.mapHandlers = map;
    }

    public void publish(OutboxEvent event) {

        OutboxEventHandler handler = mapHandlers.get(event.getEventType());

        if (handler == null)
            throw new IllegalStateException("Nenhum handler encontrado para " + event.getEventType());

        handler.handle(event);
    }
}