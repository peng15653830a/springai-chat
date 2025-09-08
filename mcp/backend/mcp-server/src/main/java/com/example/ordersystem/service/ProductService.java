package com.example.ordersystem.service;

import com.example.ordersystem.entity.Product;
import com.example.ordersystem.entity.User;
import com.example.ordersystem.repository.ProductRepository;
import com.example.ordersystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    public Product findByName(String name) {
        return productRepository.findByName(name).orElse(null);
    }

    // 添加MCP工具需要的方法
    public List<Product> findByNameContaining(String name) {
        return productRepository.findAll().stream()
            .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()))
            .toList();
    }
}