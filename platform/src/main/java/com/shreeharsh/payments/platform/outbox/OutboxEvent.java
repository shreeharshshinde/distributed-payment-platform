package com.shreeharsh.payments.platform.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", indexes = {
        @Index(name = "idx_outbox_status", columnList = "status"),
        @Index(name = "idx_outbox_aggregate", columnList = "aggregateId")
})
public class OutboxEvent {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Aggregate root this event belongs to
     * (Payment ID in our case)
     */
    @Column(nullable = false)
    private UUID aggregateId;

    /**
     * Logical event type
     * e.g. PAYMENT_AUTHORIZATION_REQUESTED
     */
    @Column(nullable = false)
    private String eventType;

    /**
     * Serialized payload (JSON)
     */
    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected OutboxEvent() {}

    public OutboxEvent(
            UUID aggregateId,
            String eventType,
            String payload
    ) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    // ---------- getters ----------

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // ---------- state transitions ----------

    public void markSent() {
        this.status = OutboxStatus.SENT;
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }
}