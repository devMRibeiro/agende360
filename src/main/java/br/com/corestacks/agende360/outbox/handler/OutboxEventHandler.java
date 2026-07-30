package br.com.corestacks.agende360.outbox.handler;

import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

public interface OutboxEventHandler {
	boolean supports(OutboxEventType eventType);

    void handle(OutboxEvent event);
}