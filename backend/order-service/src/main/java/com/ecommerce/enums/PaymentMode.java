package com.ecommerce.enums;

public enum PaymentMode {
    CASH_ON_DELIVERY("Cash on Delivery"),
    WALLET("E-Wallet"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI("UPI");
    
    private final String displayName;
    
    PaymentMode(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}