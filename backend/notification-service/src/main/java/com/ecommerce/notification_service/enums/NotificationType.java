package com.ecommerce.notification_service.enums;

public enum NotificationType {
    ORDER_PLACED("Order Placed"),
    ORDER_PENDING("Order Pending"),
    ORDER_CONFIRMED("Order Confirmed"),
    ORDER_PROCESSING("Order Processing"),
    ORDER_DISPATCHED("Order Dispatched"),
    ORDER_OUT_FOR_DELIVERY("Order Out For Delivery"),
    ORDER_DELIVERED("Order Delivered"),
    ORDER_CANCELLED("Order Cancelled"),
    ORDER_RETURNED("Order Returned"),
    PAYMENT_SUCCESS("Payment Successful"),
    PAYMENT_FAILED("Payment Failed"),
    WALLET_CREDITED("Wallet Credited"),
    WALLET_DEBITED("Wallet Debited"),
    LOW_STOCK("Low Stock Alert"),
    REVIEW_RESPONSE("Review Response"),
    WELCOME("Welcome to BookNest"),
    PROMOTIONAL("Promotional Offer");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
