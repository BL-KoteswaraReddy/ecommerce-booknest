package com.ecommerce.notification_service.controller;

import com.ecommerce.notification_service.dto.ApiResponse;
import com.ecommerce.notification_service.dto.NotificationDTO;
import com.ecommerce.notification_service.dto.SendNotificationRequest;
import com.ecommerce.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    // Send notification (internal use or admin)
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationDTO>> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        NotificationDTO notification = notificationService.sendNotification(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification sent successfully", notification));
    }
    
    // Get user notifications
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notifications retrieved", notifications));
    }
    
    // Mark notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        NotificationDTO notification = notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification marked as read", notification));
    }
    
    // Mark all as read
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "All notifications marked as read", null));
    }
    
    // Delete notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Notification deleted", null));
    }
    
    // Get unread count
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long userId) {
        Long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Unread count retrieved", count));
    }
}
