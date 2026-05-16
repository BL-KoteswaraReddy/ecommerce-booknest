package com.ecommerce.wallet_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletPaymentRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Amount is required")
    private Double amount;
    
    @NotNull(message = "Order number is required")
    private String orderNumber;

    private String userEmail;
}
