package com.ecommerce.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String fullName;
    private String mobile;
    private String alternateMobile;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String addressType;
    private Boolean isDefault;
}