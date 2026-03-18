package com.example.trading_engine_backend.payload;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioDto {
    private Long stockId;
    private String symbol;
    private String companyName;
    private Integer quantity;
}
