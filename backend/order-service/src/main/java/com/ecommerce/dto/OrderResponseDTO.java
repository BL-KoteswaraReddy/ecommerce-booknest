package com.ecommerce.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private List<OrderDTO> content;
    private int pageNo;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
}
