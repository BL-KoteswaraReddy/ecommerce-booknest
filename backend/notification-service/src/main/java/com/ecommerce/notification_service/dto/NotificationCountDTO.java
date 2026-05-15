package com.ecommerce.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCountDTO {
    private Long userId;
    private Long unreadCount;
    private Long totalCount;
}