package com.ecommerce.notification_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private Long userId;
    private String userEmail;
    private String type;
    private String title;
    private String message;
    private String referenceId;
    private Boolean sendEmail;
}
