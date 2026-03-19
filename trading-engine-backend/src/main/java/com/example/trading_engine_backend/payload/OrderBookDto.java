package com.example.trading_engine_backend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderBookDto {
    private String symbol;
    private List<OrderBookEntry> bids;
    private List<OrderBookEntry> asks;
}
