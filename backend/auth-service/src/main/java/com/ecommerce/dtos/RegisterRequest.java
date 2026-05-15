package com.ecommerce.dtos;

import com.ecommerce.entity.Users;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String mobile;
    private String role;
}