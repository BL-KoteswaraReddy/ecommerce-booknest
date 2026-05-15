package com.ecommerce.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletPaymentResponse {
    private boolean success;
    private String message;
    private Double balanceAfter;
}
