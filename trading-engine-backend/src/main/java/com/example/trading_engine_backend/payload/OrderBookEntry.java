package com.example.trading_engine_backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderBookEntry {
    private BigDecimal price;
    private int quantity;
}
