package com.ecommerce.notification_service.messaging;

import com.ecommerce.notification_service.dto.SendNotificationRequest;
import com.ecommerce.notification_service.dto.event.NotificationEvent;
import com.ecommerce.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbitmq.notification-queue}")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("Received notification event {} for user {}", event.getType(), event.getUserId());

        boolean sendEmail = Boolean.TRUE.equals(event.getSendEmail()) && event.getUserEmail() != null;
        notificationService.sendNotification(new SendNotificationRequest(
                event.getUserId(),
                event.getUserEmail(),
                event.getType(),
                event.getTitle(),
                event.getMessage(),
                event.getReferenceId(),
                sendEmail
        ));
    }
}
