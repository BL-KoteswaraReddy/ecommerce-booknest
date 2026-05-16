package com.ecommerce.notification_service.service.impl;

import com.ecommerce.notification_service.dto.NotificationDTO;
import com.ecommerce.notification_service.dto.SendNotificationRequest;
import com.ecommerce.notification_service.entity.Notification;
import com.ecommerce.notification_service.enums.NotificationType;
import com.ecommerce.notification_service.exception.NotificationServiceException;
import com.ecommerce.notification_service.repository.NotificationRepository;
import com.ecommerce.notification_service.service.EmailService;
import com.ecommerce.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    
    @Override
    public NotificationDTO sendNotification(SendNotificationRequest request) {
        log.info("Sending notification to user: {}", request.getUserId());
        
        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(NotificationType.valueOf(request.getType()));
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setReferenceId(request.getReferenceId());
        
        Notification saved = notificationRepository.save(notification);
        
        // Send email if requested
        if (request.getSendEmail() && request.getUserEmail() != null) {
            emailService.sendEmail(
                request.getUserEmail(),
                request.getTitle(),
                request.getMessage()
            );
        }
        
        return convertToDTO(saved);
    }
    
    @Override
    public List<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public NotificationDTO markAsRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(userId, notificationId);
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationServiceException("Notification not found"));
        return convertToDTO(notification);
    }
    
    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
    
    @Override
    public void deleteNotification(Long userId, Long notificationId) {
        notificationRepository.deleteByUserIdAndId(userId, notificationId);
    }
    
    @Override
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    @Override
    public void sendOrderPlacedNotification(Long userId, String userEmail, String orderNumber) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "ORDER_PLACED",
            "Order Placed Successfully",
            "Your order #" + orderNumber + " has been placed successfully. We will notify you once it's confirmed.",
            orderNumber, true
        ));
    }
    
    @Override
    public void sendOrderStatusUpdateNotification(Long userId, String userEmail, String orderNumber, String status) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "ORDER_" + status.toUpperCase(),
            "Order " + status,
            "Your order #" + orderNumber + " has been " + status.toLowerCase() + ".",
            orderNumber, true
        ));
    }
    
    @Override
    public void sendPaymentSuccessNotification(Long userId, String userEmail, String orderNumber, Double amount) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "PAYMENT_SUCCESS",
            "Payment Successful",
            "Your payment of ₹" + amount + " for order #" + orderNumber + " has been successfully processed.",
            orderNumber, true
        ));
    }
    
    @Override
    public void sendPaymentFailedNotification(Long userId, String userEmail, String orderNumber, Double amount) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "PAYMENT_FAILED",
            "Payment Failed",
            "Your payment of ₹" + amount + " for order #" + orderNumber + " failed. Please try again.",
            orderNumber, true
        ));
    }
    
    @Override
    public void sendWalletCreditedNotification(Long userId, String userEmail, Double amount, Double balance) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "WALLET_CREDITED",
            "Wallet Credited",
            "₹" + amount + " has been added to your wallet. Current balance: ₹" + balance,
            null, true
        ));
    }
    
    @Override
    public void sendWalletDebitedNotification(Long userId, String userEmail, Double amount, Double balance) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "WALLET_DEBITED",
            "Wallet Debited",
            "₹" + amount + " has been debited from your wallet. Current balance: ₹" + balance,
            null, true
        ));
    }
    
    @Override
    public void sendWelcomeNotification(Long userId, String userEmail, String userName) {
        sendNotification(new SendNotificationRequest(
            userId, userEmail, "WELCOME",
            "Welcome to BookNest!",
            "Hi " + userName + ", welcome to BookNest! Start exploring millions of books and find your next great read.",
            null, true
        ));
    }
    
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setIsRead(notification.getIsRead());
        dto.setReferenceId(notification.getReferenceId());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}