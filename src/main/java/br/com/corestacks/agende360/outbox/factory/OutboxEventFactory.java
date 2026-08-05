package br.com.corestacks.agende360.outbox.factory;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.outbox.enums.AggregateType;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;
import br.com.corestacks.agende360.outbox.model.OutboxStatus;

@Component
public class OutboxEventFactory {

	private final ObjectMapper objectMapper;

	public OutboxEventFactory(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}
	
    public OutboxEvent create(AggregateType aggregateType, UUID aggregateId, OutboxEventType eventType, Object payload) {
        return OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(objectMapper.convertValue(payload, JsonNode.class))
                .eventStatus(OutboxStatus.PENDING)
                .createdAt(Instant.now())
                .retryCount(0)
                .build();
    }
}