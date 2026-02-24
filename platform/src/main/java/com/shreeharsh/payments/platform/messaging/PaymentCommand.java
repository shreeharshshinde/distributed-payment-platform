package com.shreeharsh.payments.platform.messaging;

import java.util.UUID;

public class PaymentCommand {

    public enum Type {
        AUTHORIZE
    }

    private UUID paymentId;
    private Type type;
    private int retryCount;

    protected PaymentCommand() {}

    public PaymentCommand(UUID paymentId, Type type) {
        this(paymentId, type, 0);
    }

    public PaymentCommand(UUID paymentId, Type type, int retryCount) {
        this.paymentId = paymentId;
        this.type = type;
        this.retryCount = retryCount;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public Type getType() {
        return type;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public PaymentCommand nextRetry() {
        return new PaymentCommand(paymentId, type, retryCount + 1);
    }
}