package com.example.trading_engine_backend.controller;

import com.example.trading_engine_backend.model.Stock;
import com.example.trading_engine_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockController {

    private final StockRepository stockRepository;

    @GetMapping
    public ResponseEntity<List<Stock>> getStocks(@RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(stockRepository.findByCategory(category));
        }
        return ResponseEntity.ok(stockRepository.findAll());
    }
}
