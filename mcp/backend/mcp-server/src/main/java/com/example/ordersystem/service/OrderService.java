package com.example.ordersystem.service;

import com.example.ordersystem.entity.Order;
import com.example.ordersystem.entity.Product;
import com.example.ordersystem.repository.OrderRepository;
import com.example.ordersystem.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
    
    public Order saveOrder(Order order) {
        // Check if product exists and has enough stock
        Optional<Product> productOpt = productRepository.findById(order.getProductId());
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStock() >= order.getQuantity()) {
                // Update stock
                product.setStock(product.getStock() - order.getQuantity());
                productRepository.save(product);
                
                // Save order
                return orderRepository.save(order);
            }
        }
        return null;
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}