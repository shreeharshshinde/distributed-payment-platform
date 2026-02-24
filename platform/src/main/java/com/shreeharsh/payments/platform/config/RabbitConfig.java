package com.shreeharsh.payments.platform.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    // Queues
    public static final String COMMAND_QUEUE = "payment.command.queue";
    public static final String RETRY_QUEUE = "payment.command.retry.queue";
    public static final String DLQ_QUEUE = "payment.command.dlq.queue";

    // Exchanges
    public static final String COMMAND_EXCHANGE = "payment.command.exchange";
    public static final String RETRY_EXCHANGE = "payment.command.retry.exchange";
    public static final String DLQ_EXCHANGE = "payment.command.dlq.exchange";

    // Routing
    public static final String ROUTING_KEY = "payment.command";

    // ---------------- Exchanges ----------------

    @Bean
    DirectExchange commandExchange() {
        return new DirectExchange(COMMAND_EXCHANGE);
    }

    @Bean
    DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }

    @Bean
    DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    // ---------------- Queues ----------------

    @Bean
    Queue commandQueue() {
        return new Queue(
                COMMAND_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-dead-letter-exchange", RETRY_EXCHANGE,
                        "x-dead-letter-routing-key", ROUTING_KEY
                )
        );
    }

    @Bean
    Queue retryQueue() {
        return new Queue(
                RETRY_QUEUE,
                true,
                false,
                false,
                Map.of(
                        "x-message-ttl", 5000,
                        "x-dead-letter-exchange", COMMAND_EXCHANGE,
                        "x-dead-letter-routing-key", ROUTING_KEY
                )
        );
    }

    @Bean
    Queue dlqQueue() {
        return new Queue(DLQ_QUEUE, true);
    }

    // ---------------- Bindings ----------------

    @Bean
    Binding commandBinding() {
        return BindingBuilder.bind(commandQueue())
                .to(commandExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(retryExchange())
                .with(ROUTING_KEY);
    }

    @Bean
    Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue())
                .to(dlqExchange())
                .with(ROUTING_KEY);
    }
}