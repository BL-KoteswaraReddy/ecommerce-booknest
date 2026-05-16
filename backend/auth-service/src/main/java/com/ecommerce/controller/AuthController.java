package com.ecommerce.controller;

import com.ecommerce.dtos.ApiResponse;
import com.ecommerce.dtos.LoginRequest;
import com.ecommerce.dtos.RegisterRequest;
import com.ecommerce.entity.Users;
import com.ecommerce.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        System.out.println("Register endpoint hit for email: " + request.getEmail());
        return authService.register(request);
    }

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<Users>> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // ✅ Fixed — @PathVariable name matches, use GET
    @GetMapping("/profile/{email}")
    public ResponseEntity<ApiResponse<Users>> getProfile(@PathVariable String email) {
        return authService.getProfile(email);
    }

    // ✅ Fixed — passwords as @RequestParam not @PathVariable
    @PutMapping("/change-password/{email}")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String email,
            @RequestParam String oldPassword,    // ✅ RequestParam
            @RequestParam String newPassword     // ✅ RequestParam
    ) {
        return authService.changePassword(email, oldPassword, newPassword);
    }

    // ✅ Fixed — added slash before userId
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long userId) {
        return authService.deleteUser(userId);
    }

    // AuthController.java — add this
    @GetMapping("/google/callback")
    public ResponseEntity<ApiResponse<Users>> googleCallback(
            @RequestParam String email,
            @RequestParam String role) {
        return authService.generateTokenForOAuthUser(email, role);
    }

}