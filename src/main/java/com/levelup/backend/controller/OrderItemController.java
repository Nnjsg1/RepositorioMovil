package com.levelup.backend.controller;

import com.levelup.backend.dto.OrderItemDTO;
import com.levelup.backend.model.OrderItem;
import com.levelup.backend.repository.OrderItemRepository;
import com.levelup.backend.repository.OrderRepository;
import com.levelup.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // Obtener todos los items de orden
    @GetMapping
    public ResponseEntity<List<OrderItemDTO>> getAllOrderItems() {
        List<OrderItemDTO> items = orderItemRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    // Obtener item de orden por ID
    @GetMapping("/{id}")
    public ResponseEntity<OrderItemDTO> getOrderItemById(@PathVariable Integer id) {
        return orderItemRepository.findById(id)
                .map(item -> ResponseEntity.ok(convertToDTO(item)))
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener items de una orden
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemDTO>> getItemsByOrder(@PathVariable Integer orderId) {
        List<OrderItemDTO> items = orderItemRepository.findByOrder_Id(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(items);
    }

    // Crear nuevo item de orden
    @PostMapping
    public ResponseEntity<OrderItemDTO> createOrderItem(@RequestBody OrderItemDTO itemDTO) {
        OrderItem item = convertToEntity(itemDTO);
        OrderItem savedItem = orderItemRepository.save(item);
        return ResponseEntity.ok(convertToDTO(savedItem));
    }

    // Actualizar item de orden
    @PutMapping("/{id}")
    public ResponseEntity<OrderItemDTO> updateOrderItem(@PathVariable Integer id, @RequestBody OrderItemDTO itemDTO) {
        return orderItemRepository.findById(id)
                .map(item -> {
                    item.setQuantity(itemDTO.getQuantity());
                    item.setPrice(itemDTO.getPrice());
                    OrderItem updatedItem = orderItemRepository.save(item);
                    return ResponseEntity.ok(convertToDTO(updatedItem));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar item de orden
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Integer id) {
        if (orderItemRepository.existsById(id)) {
            orderItemRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Convertir OrderItem a OrderItemDTO
    private OrderItemDTO convertToDTO(OrderItem item) {
        return new OrderItemDTO(item.getId(), item.getOrder().getId(), item.getProduct().getId(), 
                              item.getQuantity(), item.getPrice());
    }

    // Convertir OrderItemDTO a OrderItem
    private OrderItem convertToEntity(OrderItemDTO itemDTO) {
        OrderItem item = new OrderItem();
        if (itemDTO.getOrderId() != null) {
            orderRepository.findById(itemDTO.getOrderId()).ifPresent(item::setOrder);
        }
        if (itemDTO.getProductId() != null) {
            productRepository.findById(itemDTO.getProductId()).ifPresent(item::setProduct);
        }
        item.setQuantity(itemDTO.getQuantity());
        item.setPrice(itemDTO.getPrice());
        return item;
    }
}
