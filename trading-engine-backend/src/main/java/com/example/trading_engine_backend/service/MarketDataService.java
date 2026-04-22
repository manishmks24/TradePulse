package com.example.trading_engine_backend.service;

import com.example.trading_engine_backend.model.Stock;
import com.example.trading_engine_backend.payload.MarketTick;
import com.example.trading_engine_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class MarketDataService {

    private final StockRepository stockRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private List<Stock> stocks;

    @PostConstruct
    public void init() {
        stocks = stockRepository.findAll();
        // Base starting prices
        for (Stock stock : stocks) {
            currentPrices.put(stock.getSymbol(), BigDecimal.valueOf(100 + random.nextInt(100)));
        }
    }

    // Run every 1 second (DISABLED for Live External Agent)
    // @Scheduled(fixedRate = 1000)
    public void generateMarketTicks() {
        // Simulation disabled. Data now ingested via MarketDataIngestionController.
    }
}
