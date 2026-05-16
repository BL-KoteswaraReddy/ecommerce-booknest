package com.ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/get")
@RestController
public class GatewayController {

    @GetMapping
    public String get()
    {
        return "Gate way is working";
    }

}
