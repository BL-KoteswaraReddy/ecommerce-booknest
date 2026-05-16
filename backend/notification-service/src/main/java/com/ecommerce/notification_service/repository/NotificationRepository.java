package com.ecommerce.notification_service.repository;

import com.ecommerce.notification_service.entity.Notification;
import com.ecommerce.notification_service.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.id = :notificationId")
    void markAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") Long userId);
    
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.id = :notificationId")
    void deleteByUserIdAndId(@Param("userId") Long userId, @Param("notificationId") Long notificationId);
    
    List<Notification> findByUserIdAndType(Long userId, NotificationType type);
    
    List<Notification> findByCreatedAtBefore(LocalDateTime date);
}