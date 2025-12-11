package com.levelup.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Integer userId;
    private Integer productId;
    private String productTitle;
    private String productImage;
    private Double productPrice;
    private String productCurrency;
    private Integer quantity;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
