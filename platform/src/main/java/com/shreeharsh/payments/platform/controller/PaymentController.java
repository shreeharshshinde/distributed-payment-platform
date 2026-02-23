package com.shreeharsh.payments.platform.controller;

import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

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

    /**
     * ASYNC authorization request
     * Returns 202 Accepted (command accepted, not completed)
     */
    @PostMapping("/{id}/authorize")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void authorize(@PathVariable UUID id) {
        paymentService.requestAuthorization(id);
    }

    /**
     * Sync capture (Stage 5 can make this async later)
     */
    @PostMapping("/{id}/capture")
    public Payment capture(@PathVariable UUID id) {
        return paymentService.capture(id);
    }
}