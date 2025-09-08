package com.example.ordersystem.controller;

import com.example.ordersystem.dto.NaturalLanguageRequest;
import com.example.ordersystem.entity.Order;
import com.example.ordersystem.service.NaturalLanguageOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nl-orders")
@CrossOrigin(origins = "*")
public class NaturalLanguageOrderController {
    
    @Autowired
    private NaturalLanguageOrderService naturalLanguageOrderService;
    
    @PostMapping
    public Order createOrderFromNaturalLanguage(@RequestBody NaturalLanguageRequest request) {
        return naturalLanguageOrderService.processNaturalLanguageOrder(request.getInstruction());  
    }
}