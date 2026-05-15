package com.ecommerce.wallet_service.enums;

public enum TransactionType {
    CREDIT("Credit"),
    DEBIT("Debit");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}