package com.example.trading_engine_backend.controller;

import com.example.trading_engine_backend.payload.PortfolioDto;
import com.example.trading_engine_backend.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<PortfolioDto>> getUserPortfolio(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getUserPortfolio(userId));
    }

    @GetMapping("/{userId}/cash")
    public ResponseEntity<BigDecimal> getUserCashBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getUserCashBalance(userId));
    }
}
