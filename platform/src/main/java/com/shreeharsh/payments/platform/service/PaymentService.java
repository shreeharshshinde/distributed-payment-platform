package com.shreeharsh.payments.platform.service;

import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.domain.PaymentStatus;
import com.shreeharsh.payments.platform.messaging.PaymentCommand;
import com.shreeharsh.payments.platform.messaging.PaymentCommandPublisher;
import com.shreeharsh.payments.platform.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCommandPublisher commandPublisher;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentCommandPublisher commandPublisher
    ) {
        this.paymentRepository = paymentRepository;
        this.commandPublisher = commandPublisher;
    }

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
     * ASYNC command dispatch
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

        commandPublisher.publish(
                new PaymentCommand(id, PaymentCommand.Type.AUTHORIZE)
        );
    }

    /**
     * SYNC capture
     */
    @Transactional
    public Payment capture(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        payment.capture();
        return paymentRepository.save(payment);
    }
}