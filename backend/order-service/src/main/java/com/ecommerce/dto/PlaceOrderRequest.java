package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // CASH_ON_DELIVERY, WALLET

    private String userEmail;
    
    @NotNull(message = "Shipping address is required")
    private AddressDTO shippingAddress;
    
    private String orderNotes;
}
