package br.com.corestacks.agende360.outbox.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

@Entity(name = "outbox_event")
public class OutboxEvent {

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "aggregate_type", nullable = false)
	private String aggregateType;

	@Column(name = "aggregate_id", nullable = false)
	private Long aggregateId;

	@Column(name = "event_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private OutboxEventType eventType;

	@Column(name = "event_version", nullable = false)
	private String eventVersion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private JsonNode payload;

	@Column(name = "event_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OutboxStatus eventStatus;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "sent_at")
	private Instant sentAt;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public Long getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(Long aggregateId) {
		this.aggregateId = aggregateId;
	}

	public OutboxEventType getEventType() {
		return eventType;
	}

	public void setEventType(OutboxEventType eventType) {
		this.eventType = eventType;
	}

	public String getEventVersion() {
		return eventVersion;
	}

	public void setEventVersion(String eventVersion) {
		this.eventVersion = eventVersion;
	}

	public JsonNode getPayload() {
		return payload;
	}

	public void setPayload(JsonNode payload) {
		this.payload = payload;
	}

	public OutboxStatus getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(OutboxStatus eventStatus) {
		this.eventStatus = eventStatus;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public void setSentAt(Instant sentAt) {
		this.sentAt = sentAt;
	}
}