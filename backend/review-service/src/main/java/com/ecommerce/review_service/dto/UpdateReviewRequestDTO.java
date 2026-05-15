package com.ecommerce.review_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateReviewRequestDTO {
    
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;
    
    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String comment;
}