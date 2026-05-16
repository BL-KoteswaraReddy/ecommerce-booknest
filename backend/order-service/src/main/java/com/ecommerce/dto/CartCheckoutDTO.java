package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class CartCheckoutDTO {
    private Long userId;
    private List<CartItemInfoDTO> items;

    @JsonProperty("totalPrice")      // ← maps cart-service "totalPrice" field
    private Double totalAmount;
    private Integer totalItems;
}