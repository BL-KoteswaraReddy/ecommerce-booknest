package com.ecommerce.services;

import com.ecommerce.dtos.ApiResponse;
import com.ecommerce.dtos.LoginRequest;
import com.ecommerce.dtos.RegisterRequest;
import com.ecommerce.entity.Users;
import org.springframework.http.ResponseEntity;


public interface AuthService {
    ResponseEntity<ApiResponse<Void>>      register(RegisterRequest request);
    ResponseEntity<ApiResponse<Users>> login(LoginRequest request);
    ResponseEntity<ApiResponse<Users>>     getProfile(String email);
    ResponseEntity<ApiResponse<Void>>      changePassword(String email, String oldPassword, String newPassword);
    ResponseEntity<ApiResponse<Void>>      deleteUser(Long userId);

    // AuthService.java interface — add this
    ResponseEntity<ApiResponse<Users>> generateTokenForOAuthUser(String email, String role);
}