package com.shreeharsh.payments.platform.messaging;

import com.shreeharsh.payments.platform.config.RabbitConfig;
import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.repository.PaymentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCommandListener {

    private final PaymentRepository repository;

    public PaymentCommandListener(PaymentRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_QUEUE)
    @Transactional
    public void handle(PaymentCommand command) throws InterruptedException {

        Payment payment = repository.findById(command.getPaymentId())
                .orElseThrow();

        // Simulate external PSP latency
        Thread.sleep(2000);

        if (command.getType() == PaymentCommand.Type.AUTHORIZE) {
            payment.authorize();
        }

        repository.save(payment);
    }
}