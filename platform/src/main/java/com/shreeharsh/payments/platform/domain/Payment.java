package com.shreeharsh.payments.platform.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = @UniqueConstraint(columnNames = "idempotencyKey")
)
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private Long version;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    protected Payment() {}

    public Payment(BigDecimal amount, String currency, String idempotencyKey) {
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.CREATED;
        this.createdAt = Instant.now();
    }

    // getters only (no setters for now)
    public UUID getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public String getIdempotencyKey() { return idempotencyKey; }

    public void authorize() {
        if (this.status != PaymentStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED payments can be authorized"
            );
        }
        this.status = PaymentStatus.AUTHORIZED;
    }

    public void capture() {
        if (this.status != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException(
                    "Only AUTHORIZED payments can be captured"
            );
        }
        this.status = PaymentStatus.CAPTURED;
    }

    public void fail() {
        if (this.status == PaymentStatus.CAPTURED) {
            throw new IllegalStateException(
                    "CAPTURED payments cannot be failed"
            );
        }
        this.status = PaymentStatus.FAILED;
    }
}