package com.example.trading_engine_backend.controller;

import com.example.trading_engine_backend.model.Trade;
import com.example.trading_engine_backend.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TradeController {

    private final TradeRepository tradeRepository;

    @GetMapping("/stock/{stockId}")
    public ResponseEntity<List<Trade>> getTradeHistory(@PathVariable Long stockId) {
        // In a real app, you'd use a DTO. For charting, full trade history works fine.
        return ResponseEntity.ok(tradeRepository.findByStockIdOrderByTimestampDesc(stockId));
    }
}
