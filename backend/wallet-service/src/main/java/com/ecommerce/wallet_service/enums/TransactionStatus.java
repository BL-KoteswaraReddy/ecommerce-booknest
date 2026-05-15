package com.ecommerce.wallet_service.enums;

public enum TransactionStatus {
    SUCCESS("Success"),
    FAILED("Failed"),
    PENDING("Pending");
    
    private final String displayName;
    
    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}