package com.shreeharsh.payments.platform.messaging;

import java.io.Serializable;
import java.util.UUID;

public class PaymentCommand implements Serializable {

    public enum Type {
        AUTHORIZE,
        CAPTURE
    }

    private UUID paymentId;
    private Type type;

    protected PaymentCommand() {}

    public PaymentCommand(UUID paymentId, Type type) {
        this.paymentId = paymentId;
        this.type = type;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public Type getType() {
        return type;
    }
}