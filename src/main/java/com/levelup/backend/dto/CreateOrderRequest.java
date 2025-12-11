package com.levelup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    private Integer userId;
    private String status;
    private Double total;
    private List<OrderItemRequest> items;
}
