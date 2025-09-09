package com.example.ordersystem.server;

import com.example.ordersystem.server.entity.Product;
import com.example.ordersystem.server.entity.User;
import com.example.ordersystem.server.repository.ProductRepository;
import com.example.ordersystem.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Initialize users
        if (userRepository.count() == 0) {
            User user1 = new User("alice");
            User user2 = new User("bob");
            userRepository.save(user1);
            userRepository.save(user2);
        }
        
        // Initialize products
        if (productRepository.count() == 0) {
            Product product1 = new Product("苹果手机", new BigDecimal("8999.00"), 100);
            Product product2 = new Product("华为手机", new BigDecimal("7999.00"), 80);
            Product product3 = new Product("小米手机", new BigDecimal("3999.00"), 150);
            Product product4 = new Product("iPad", new BigDecimal("4999.00"), 50);
            Product product5 = new Product("MacBook", new BigDecimal("12999.00"), 30);
            
            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            productRepository.save(product4);
            productRepository.save(product5);
        }
    }
}