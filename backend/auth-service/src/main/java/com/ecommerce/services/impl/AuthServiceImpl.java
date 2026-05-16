package com.ecommerce.services.impl;

import com.ecommerce.dtos.ApiResponse;
import com.ecommerce.dtos.LoginRequest;
import com.ecommerce.dtos.RegisterRequest;
import com.ecommerce.entity.Users;
import com.ecommerce.util.JwtUtil;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static java.util.stream.DoubleStream.builder;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    public ResponseEntity<ApiResponse<Void>> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.<Void>builder()
                            .status(409)
                            .message("Email already registered")
                            .data(null)
                            .build());
        }

        Users users = Users.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile())
                .role(request.getRole() != null ? request.getRole() : "CUSTOMER")
                .build();

           userRepository.save(users);

        // No token here — just success message
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .status(201)
                        .message("Registration successful. Please login.")
                        .data(null)
                        .build());
    }


    //login method
    public ResponseEntity<ApiResponse<Users>> login(LoginRequest request) {
       //check user exists
        Users user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if(user == null)
        {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Users>builder()
                            .status(404)
                            .message("User not found")
                            .data(null)
                            .build());
        }

        //check password
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Users>builder()
                      .status(401)
                      .message("Invalid password")
                      .data(null)
                      .build());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        user.setToken(token);          // ✅ Set token directly on user object

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<Users>builder()
                        .status(200)
                        .message("Login successful")
                        .data(user)    // ✅ user already has token inside it
                        .build());

    }



    @Override
    public ResponseEntity<ApiResponse<Users>> getProfile(String email) {

        Users user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Users>builder()
                            .status(404)
                            .message("User not found with email: " + email)
                            .data(null)
                            .build());
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<Users>builder()
                        .status(200)
                        .message("Profile fetched successfully")
                        .data(user)
                        .build());
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> changePassword(String email,
                                                            String oldPassword,
                                                            String newPassword) {
        Users user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .status(404)
                            .message("User not found")
                            .data(null)
                            .build());
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Void>builder()
                            .status(401)
                            .message("Old password is incorrect")
                            .data(null)
                            .build());
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<Void>builder()
                        .status(200)
                        .message("Password changed successfully")
                        .data(null)
                        .build());
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .status(404)
                            .message("User not found with id: " + userId)
                            .data(null)
                            .build());
        }

        userRepository.deleteById(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<Void>builder()
                        .status(200)
                        .message("User deleted successfully")
                        .data(null)
                        .build());

    }


    //Oauth implementation
    @Override
    public ResponseEntity<ApiResponse<Users>> generateTokenForOAuthUser(String email, String role) {
        Users user = userRepository.findByEmail(email).orElse(null);

        if(user == null)
        {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Users>builder()
                            .status(404)
                            .message("User not found")
                            .data(null)
                            .build());
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        user.setToken(token);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<Users>builder()
                .status(200)
                .message("Google login successful")
                .data(user)
                .build());
    }
}
