package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    private String mobile;
    
    @Column(name = "alternate_mobile")
    private String alternateMobile;
    
    @Column(nullable = false)
    private String address;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;
    
    @Column(name = "postal_code", nullable = false)
    private String postalCode;
    
    @Column(nullable = false)
    private String country;
    
    @Column(name = "address_type")
    private String addressType = "Home";
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
}
