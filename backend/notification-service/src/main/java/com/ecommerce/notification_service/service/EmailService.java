package com.ecommerce.notification_service.service;

import com.ecommerce.notification_service.entity.EmailLog;
import com.ecommerce.notification_service.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public boolean sendEmail(String to, String subject, String body) {
        EmailLog emailLog = new EmailLog();
        emailLog.setToEmail(to);
        emailLog.setSubject(subject);
        emailLog.setBody(body);
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            
            emailLog.setIsSent(true);
            emailLog.setSentAt(java.time.LocalDateTime.now());
            emailLogRepository.save(emailLog);
            
            log.info("Email sent to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            emailLog.setIsSent(false);
            emailLog.setErrorMessage(e.getMessage());
            emailLogRepository.save(emailLog);
            return false;
        }
    }
}