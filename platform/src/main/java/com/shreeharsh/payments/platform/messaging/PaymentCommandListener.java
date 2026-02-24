package com.shreeharsh.payments.platform.messaging;

import com.shreeharsh.payments.platform.config.RabbitConfig;
import com.shreeharsh.payments.platform.domain.Payment;
import com.shreeharsh.payments.platform.repository.PaymentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCommandListener {

    private static final int MAX_RETRIES = 3;

    private final PaymentRepository repository;
    private final PaymentCommandPublisher publisher;

    public PaymentCommandListener(
            PaymentRepository repository,
            PaymentCommandPublisher publisher
    ) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @RabbitListener(queues = RabbitConfig.COMMAND_QUEUE)
    @Transactional
    public void handle(PaymentCommand command) throws InterruptedException {

        Payment payment = repository.findById(command.getPaymentId())
                .orElseThrow();

        try {
            Thread.sleep(2000); // PSP simulation

            if (command.getType() == PaymentCommand.Type.AUTHORIZE) {
                payment.authorize();
            }

            repository.save(payment);

        } catch (Exception ex) {

            if (command.getRetryCount() >= MAX_RETRIES) {
                payment.fail();
                repository.save(payment);

                publisher.publishToDlq(command);
                return;
            }

            publisher.publishRetry(command.nextRetry());
        }
    }
}