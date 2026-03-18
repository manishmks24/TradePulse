package com.example.trading_engine_backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class MarketTick {
    private String symbol;
    private BigDecimal price;
    private Instant timestamp;
}
