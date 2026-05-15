package com.ecommerce.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T>
{
    private int status;  //status codes;
    private String message;  //ex: login successful
    private T data; //any object - User, token, null;
}