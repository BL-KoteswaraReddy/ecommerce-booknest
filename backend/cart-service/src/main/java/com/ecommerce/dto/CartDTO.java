package com.ecommerce.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private Long userId;
    private Double totalPrice;
    private Integer totalItems;
    private List<CartItemDTO> items;
    private String createdAt;
    private String updatedAt;
}
