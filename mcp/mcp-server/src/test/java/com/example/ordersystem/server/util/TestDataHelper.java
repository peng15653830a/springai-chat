package com.example.ordersystem.server.util;

import com.example.ordersystem.server.entity.Order;
import com.example.ordersystem.server.entity.Product;
import com.example.ordersystem.server.entity.User;
import com.example.ordersystem.server.repository.OrderRepository;
import com.example.ordersystem.server.repository.ProductRepository;
import com.example.ordersystem.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TestDataHelper {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public User createTestUser(String username) {
        User user = new User();
        user.setUsername(username);
        return userRepository.save(user);
    }
    
    public Product createTestProduct(String name, BigDecimal price, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        return productRepository.save(product);
    }
    
    public Order createTestOrder(Long userId, Long productId, Integer quantity, BigDecimal totalPrice) {
        Order order = new Order(userId, productId, quantity, totalPrice);
        return orderRepository.save(order);
    }
    
    public void cleanupTestData() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
    
    public Product findProductByName(String name) {
        return productRepository.findByName(name).orElse(null);
    }
}