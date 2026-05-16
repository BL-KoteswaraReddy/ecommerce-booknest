package com.ecommerce.messaging;

import com.ecommerce.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String notificationRoutingKey;

    public void publish(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, notificationRoutingKey, event);
            log.info("Published notification event {} for user {}", event.getType(), event.getUserId());
        } catch (AmqpException ex) {
            log.error("Failed to publish notification event {} for user {}: {}",
                    event.getType(), event.getUserId(), ex.getMessage());
        }
    }
}
