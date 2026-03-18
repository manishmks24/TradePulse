package com.example.trading_engine_backend.payload;

import com.example.trading_engine_backend.model.enums.OrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    private Long userId;
    private Long stockId;
    private OrderType type;
    private BigDecimal price;
    private Integer quantity;
}
