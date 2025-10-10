package com.example.ordersystem.server.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.ordersystem.server.entity.Order;
import com.example.ordersystem.server.entity.Product;
import com.example.ordersystem.server.service.OrderService;
import com.example.ordersystem.server.service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderSystemTools {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ProductService productService;
    
    @Tool(name = "create_order", description = "创建一个新的订单")
    public Map<String, Object> createOrder(
        @ToolParam(description = "产品名称") String productName,
        @ToolParam(description = "数量") Integer quantity,
        @ToolParam(description = "用户ID") Long userId) {
        try {
            // 查找产品
            Product product = productService.findByName(productName);
            if (product == null) {
                return Map.of("error", "产品不存在: " + productName);
            }
            
            // 检查库存
            if (product.getStock() < quantity) {
                return Map.of("error", "库存不足，当前库存: " + product.getStock());
            }
            
            // 创建订单
            Order order = new Order(userId, product.getId(), quantity, 
                product.getPrice().multiply(java.math.BigDecimal.valueOf(quantity)));
            Order savedOrder = orderService.saveOrder(order);
            
            return Map.of(
                "success", true,
                "order_id", savedOrder.getId(),
                "product_name", productName,
                "quantity", quantity,
                "total_price", savedOrder.getTotalPrice()
            );
        } catch (Exception e) {
            return Map.of("error", "创建订单失败: " + e.getMessage());
        }
    }
    
    @Tool(name = "get_order", description = "根据订单ID获取订单详情")
    public Map<String, Object> getOrder(@ToolParam(description = "订单ID") Long orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            
            if (!orderOpt.isPresent()) {
                return Map.of("error", "订单不存在: " + orderId);
            }
            
            Order order = orderOpt.get();
            return Map.of(
                "order_id", order.getId(),
                "user_id", order.getUserId(),
                "product_id", order.getProductId(),
                "quantity", order.getQuantity(),
                "total_price", order.getTotalPrice(),
                "status", order.getStatus(),
                "created_at", order.getCreatedAt()
            );
        } catch (Exception e) {
            return Map.of("error", "获取订单失败: " + e.getMessage());
        }
    }
    
    @Tool(name = "search_products", description = "根据产品名称搜索产品")
    public Map<String, Object> searchProducts(@ToolParam(description = "产品名称关键词") String name) {
        try {
            List<Product> products = productService.findByNameContaining(name);
            
            return Map.of(
                "products", products.stream()
                    .map(p -> Map.of(
                        "id", p.getId(),
                        "name", p.getName(),
                        "price", p.getPrice(),
                        "stock", p.getStock()
                    ))
                    .toList()
            );
        } catch (Exception e) {
            return Map.of("error", "搜索产品失败: " + e.getMessage());
        }
    }
    
    @Tool(name = "get_orders", description = "获取所有订单列表")
    public Map<String, Object> getOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            
            return Map.of(
                "orders", orders.stream()
                    .map(o -> Map.of(
                        "id", o.getId(),
                        "user_id", o.getUserId(),
                        "product_id", o.getProductId(),
                        "quantity", o.getQuantity(),
                        "total_price", o.getTotalPrice(),
                        "status", o.getStatus(),
                        "created_at", o.getCreatedAt()
                    ))
                    .toList()
            );
        } catch (Exception e) {
            return Map.of("error", "获取订单列表失败: " + e.getMessage());
        }
    }
}