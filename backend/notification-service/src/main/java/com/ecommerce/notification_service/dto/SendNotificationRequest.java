package com.ecommerce.notification_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "User email is required")
    private String userEmail;
    
    @NotNull(message = "Notification type is required")
    private String type;
    
    @NotNull(message = "Title is required")
    private String title;
    
    @NotNull(message = "Message is required")
    private String message;
    
    private String referenceId;
    
    private Boolean sendEmail = true;
}
