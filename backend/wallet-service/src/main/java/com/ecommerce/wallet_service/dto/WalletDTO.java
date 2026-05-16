package com.ecommerce.wallet_service.dto;

import lombok.Data;

@Data
public class WalletDTO {
    private Long id;
    private Long userId;
    private Double currentBalance;
    private Double totalCredited;
    private Double totalDebited;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
}