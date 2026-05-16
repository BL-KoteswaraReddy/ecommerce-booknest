package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.dto.NotificationDTO;
import com.ecommerce.notification_service.dto.SendNotificationRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface NotificationService {
    NotificationDTO sendNotification(SendNotificationRequest request);
    List<NotificationDTO> getUserNotifications(Long userId, Pageable pageable);
    NotificationDTO markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
    void deleteNotification(Long userId, Long notificationId);
    Long getUnreadCount(Long userId);
    
    // Template methods for common events
    void sendOrderPlacedNotification(Long userId, String userEmail, String orderNumber);
    void sendOrderStatusUpdateNotification(Long userId, String userEmail, String orderNumber, String status);
    void sendPaymentSuccessNotification(Long userId, String userEmail, String orderNumber, Double amount);
    void sendPaymentFailedNotification(Long userId, String userEmail, String orderNumber, Double amount);
    void sendWalletCreditedNotification(Long userId, String userEmail, Double amount, Double balance);
    void sendWalletDebitedNotification(Long userId, String userEmail, Double amount, Double balance);
    void sendWelcomeNotification(Long userId, String userEmail, String userName);
}