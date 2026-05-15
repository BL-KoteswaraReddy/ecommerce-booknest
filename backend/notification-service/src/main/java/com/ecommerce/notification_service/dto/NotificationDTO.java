package com.ecommerce.notification_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private String referenceId;
    private LocalDateTime createdAt;
}