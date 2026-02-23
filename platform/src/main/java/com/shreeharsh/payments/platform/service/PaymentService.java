package com.shreeharsh.payments.platform.service;

import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
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
}