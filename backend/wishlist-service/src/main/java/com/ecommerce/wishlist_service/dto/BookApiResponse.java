// new file: BookApiResponse.java
package com.ecommerce.wishlist_service.dto;

import lombok.Data;

@Data
public class BookApiResponse {
    private boolean success;
    private String message;
    private BookInfoDTO data;
}