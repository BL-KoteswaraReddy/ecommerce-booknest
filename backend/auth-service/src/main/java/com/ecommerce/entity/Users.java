package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder   //check the use of this
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    private String role;                   // CUSTOMER, ADMIN

    private String mobile;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Transient                    // ✅ Not saved to DB — only in JSON response
    private String token;



    // ✅ Add these two fields below 2 fields for Google Authentication
    private String provider;          // LOCAL, GOOGLE

    private String providerId;
}