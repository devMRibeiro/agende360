package br.com.corestacks.agende360.outbox.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.corestacks.agende360.outbox.enums.AggregateType;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

	@Id
	@Column(nullable = false)
	private UUID id;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

	@Column(name = "aggregate_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private AggregateType aggregateType;

	@Column(name = "event_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private OutboxEventType eventType;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "payload", columnDefinition = "jsonb", nullable = false)
	private JsonNode payload;

	@Column(name = "event_status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OutboxStatus eventStatus;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "sent_at")
	private Instant sentAt;
	
	@Column(name = "retry_count")
	private Integer retryCount;
	
	@Column(name = "next_attempt_at")
	private LocalDateTime nextAttemptAt;
	
	@Column(name = "last_error")
	private String lastError;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public AggregateType getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(AggregateType aggregateType) {
		this.aggregateType = aggregateType;
	}

	public UUID getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(UUID aggregateId) {
		this.aggregateId = aggregateId;
	}

	public OutboxEventType getEventType() {
		return eventType;
	}

	public void setEventType(OutboxEventType eventType) {
		this.eventType = eventType;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public void setSentAt(Instant sentAt) {
		this.sentAt = sentAt;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public LocalDateTime getNextAttemptAt() {
		return nextAttemptAt;
	}

	public void setNextAttemptAt(LocalDateTime nextAttemptAt) {
		this.nextAttemptAt = nextAttemptAt;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}
	
	public void calculeNextAttempt() {
		nextAttemptAt = LocalDateTime.now().plusSeconds(calculateBackoff(retryCount));
	}
	
	private long calculateBackoff(int retryCount) {
	    return Math.min((long) Math.pow(2, retryCount) * 30, 3600);
	}
	
	public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final OutboxEvent event;

        private Builder() {
            this.event = new OutboxEvent();
        }

        public Builder id(UUID id) {
            event.id = id;
            return this;
        }

        public Builder aggregateType(AggregateType aggregateType) {
            event.aggregateType = aggregateType;
            return this;
        }

        public Builder aggregateId(UUID aggregateId) {
            event.aggregateId = aggregateId;
            return this;
        }

        public Builder eventType(OutboxEventType eventType) {
            event.eventType = eventType;
            return this;
        }

        public Builder payload(JsonNode payload) {
            event.payload = payload;
            return this;
        }

        public Builder eventStatus(OutboxStatus eventStatus) {
            event.eventStatus = eventStatus;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            event.createdAt = createdAt;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            event.retryCount = retryCount;
            return this;
        }
        
        public Builder nextAttemptAt(LocalDateTime nextAttemptAt) {
        	event.nextAttemptAt = nextAttemptAt;
        	return this;
        }

        public OutboxEvent build() {
            return event;
        }
    }
}