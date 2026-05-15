package com.ecommerce.wallet_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMoneyRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.0", message = "Minimum add amount is ₹10")
    private Double amount;

    private String userEmail;
    
    private String remarks;
}
