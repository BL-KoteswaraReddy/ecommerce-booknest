package com.ecommerce.wallet_service.dto;

import lombok.Data;

@Data
public class TransactionDTO {
    private Long id;
    private String transactionId;
    private String type;
    private Double amount;
    private Double balanceBefore;
    private Double balanceAfter;
    private String status;
    private String description;
    private String referenceId;
    private String orderNumber;
    private String transactionDate;
    private String remarks;
}