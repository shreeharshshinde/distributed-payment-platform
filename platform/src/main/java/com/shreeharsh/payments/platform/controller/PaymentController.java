package com.shreeharsh.payments.platform.controller;

import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public Payment createPayment(
            @RequestParam BigDecimal amount,
            @RequestParam String currency,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        return paymentService.createPayment(amount, currency, idempotencyKey);
    }
}