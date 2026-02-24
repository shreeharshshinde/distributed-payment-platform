package com.shreeharsh.payments.platform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.domain.PaymentStatus;
import com.shreeharsh.payments.platform.outbox.OutboxEvent;
import com.shreeharsh.payments.platform.outbox.OutboxEventRepository;
import com.shreeharsh.payments.platform.outbox.PaymentEventTypes;
import com.shreeharsh.payments.platform.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Create payment with idempotency guarantee
     */
    @Transactional
    public Payment createPayment(
            BigDecimal amount,
            String currency,
            String idempotencyKey
    ) {
        return paymentRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> {
                    Payment payment = new Payment(amount, currency, idempotencyKey);
                    return paymentRepository.save(payment);
                });
    }

    /**
     * ASYNC authorization request using Outbox pattern
     * No RabbitMQ call here
     */
    @Transactional
    public void requestAuthorization(UUID id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED payments can be authorized"
            );
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(
                    Map.of(
                            "paymentId", payment.getId(),
                            "amount", payment.getAmount(),
                            "currency", payment.getCurrency()
                    )
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }

        OutboxEvent event = new OutboxEvent(
                payment.getId(),
                PaymentEventTypes.PAYMENT_AUTHORIZATION_REQUESTED,
                payload
        );

        outboxEventRepository.save(event);
    }

    /**
     * Sync capture (still synchronous for now)
     */
    @Transactional
    public Payment capture(UUID id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.capture();
        return paymentRepository.save(payment);
    }
}