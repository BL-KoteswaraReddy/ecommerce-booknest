package com.ecommerce.notification_service.repository;

import com.ecommerce.notification_service.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findByIsSentFalse();
    List<EmailLog> findBySentAtBefore(LocalDateTime date);
}