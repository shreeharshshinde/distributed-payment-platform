package com.shreeharsh.payments.platform.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeharsh.payments.platform.messaging.PaymentCommand;
import com.shreeharsh.payments.platform.messaging.PaymentCommandPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPoller {

    private final OutboxEventRepository repository;
    private final PaymentCommandPublisher publisher;
    private final ObjectMapper objectMapper;

    public OutboxPoller(
            OutboxEventRepository repository,
            PaymentCommandPublisher publisher,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Runs every second
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void poll() {

        List<OutboxEvent> events =
                repository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : events) {
            try {
                dispatch(event);
                event.markSent();
            } catch (Exception ex) {
                // DO NOT mark as sent
                // retry on next poll
                ex.printStackTrace();
            }
        }
    }

    private void dispatch(OutboxEvent event) throws Exception {

        if (event.getEventType()
                .equals("PAYMENT_AUTHORIZATION_REQUESTED")) {

            PaymentCommand command =
                    objectMapper.readValue(
                            event.getPayload(),
                            PaymentCommand.class
                    );

            publisher.publish(command);
        }
    }
}