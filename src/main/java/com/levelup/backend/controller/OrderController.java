package com.levelup.backend.controller;

import com.levelup.backend.dto.OrderDTO;
import com.levelup.backend.dto.OrderItemDTO;
import com.levelup.backend.dto.CreateOrderRequest;
import com.levelup.backend.dto.OrderItemRequest;
import com.levelup.backend.model.Order;
import com.levelup.backend.model.OrderItem;
import com.levelup.backend.model.Product;
import com.levelup.backend.repository.OrderRepository;
import com.levelup.backend.repository.OrderItemRepository;
import com.levelup.backend.repository.UserRepository;
import com.levelup.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Transactional
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // Obtener todas las órdenes
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    // Obtener orden por ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer id) {
        return orderRepository.findById(id)
                .map(order -> ResponseEntity.ok(convertToDTO(order)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener órdenes de un usuario
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable Integer userId) {
        List<OrderDTO> orders = orderRepository.findByUser_Id(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    // Crear nueva orden
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        Order order = new Order();
        userRepository.findById(createOrderRequest.getUserId())
                .ifPresent(order::setUser);
        order.setStatus(createOrderRequest.getStatus() != null ? createOrderRequest.getStatus() : "pending");
        order.setTotal(createOrderRequest.getTotal());
        
        Order savedOrder = orderRepository.save(order);
        
        // Crear los items de la orden
        if (createOrderRequest.getItems() != null && !createOrderRequest.getItems().isEmpty()) {
            for (OrderItemRequest itemRequest : createOrderRequest.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(savedOrder);
                productRepository.findById(itemRequest.getProductId().intValue())
                        .ifPresent(orderItem::setProduct);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(itemRequest.getPrice());
                orderItemRepository.save(orderItem);
            }
        }
        
        return ResponseEntity.ok(convertToDTO(savedOrder));
    }

    // Actualizar orden
    @PutMapping("/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer id, @RequestBody OrderDTO orderDTO) {
        return orderRepository.findById(id)
                .map(order -> {
                    order.setStatus(orderDTO.getStatus());
                    order.setTotal(orderDTO.getTotal());
                    Order updatedOrder = orderRepository.save(order);
                    return ResponseEntity.ok(convertToDTO(updatedOrder));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar orden
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Convertir Order a OrderDTO
    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> items = order.getItems() != null ?
                order.getItems().stream()
                        .map(item -> new OrderItemDTO(item.getId(), item.getOrder().getId(), 
                                                     item.getProduct().getId(), item.getQuantity(), item.getPrice()))
                        .collect(Collectors.toList()) : List.of();

        return new OrderDTO(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getTotal(),
                order.getCreatedAt(),
                items
        );
    }

    // Convertir OrderDTO a Order
    private Order convertToEntity(OrderDTO orderDTO) {
        Order order = new Order();
        if (orderDTO.getUserId() != null) {
            userRepository.findById(orderDTO.getUserId()).ifPresent(order::setUser);
        }
        order.setStatus(orderDTO.getStatus());
        order.setTotal(orderDTO.getTotal());
        return order;
    }
}
