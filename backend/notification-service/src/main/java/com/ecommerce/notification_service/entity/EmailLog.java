package com.ecommerce.notification_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "to_email", nullable = false)
    private String toEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(nullable = false, length = 2000)
    private String body;
    
    @Column(name = "is_sent")
    private Boolean isSent = false;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @PrePersist
    protected void onCreate() {
        if (isSent == null) isSent = false;
    }
}