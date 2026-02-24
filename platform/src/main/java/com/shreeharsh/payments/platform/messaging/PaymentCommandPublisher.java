package com.shreeharsh.payments.platform.messaging;

import com.shreeharsh.payments.platform.config.RabbitConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    public PaymentCommandPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(PaymentCommand command) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.COMMAND_EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                command
        );
    }

    public void publishRetry(PaymentCommand command) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.RETRY_EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                command
        );
    }

    public void publishToDlq(PaymentCommand command) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.DLQ_EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                command
        );
    }
}