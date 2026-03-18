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

    // Run every 1 second
    @Scheduled(fixedRate = 1000)
    public void generateMarketTicks() {
        if (stocks == null || stocks.isEmpty()) {
            stocks = stockRepository.findAll();
            if (stocks.isEmpty()) return;
            
            // Re-initialize base prices if just loaded
            for (Stock stock : stocks) {
                if (!currentPrices.containsKey(stock.getSymbol())) {
                    currentPrices.put(stock.getSymbol(), BigDecimal.valueOf(100 + random.nextInt(100)));
                }
            }
        }

        for (Stock stock : stocks) {
            BigDecimal currentPrice = currentPrices.get(stock.getSymbol());
            // Random walk: -1% to +1% change
            double changePercent = (random.nextDouble() * 2 - 1) * 0.01;
            BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));
            BigDecimal newPrice = currentPrice.add(change).setScale(2, RoundingMode.HALF_UP);
            
            // Prevent price from going to zero
            if (newPrice.compareTo(BigDecimal.ONE) < 0) {
                newPrice = BigDecimal.ONE;
            }
            
            currentPrices.put(stock.getSymbol(), newPrice);

            MarketTick tick = new MarketTick(stock.getSymbol(), newPrice, Instant.now());
            messagingTemplate.convertAndSend("/topic/market/" + stock.getSymbol(), tick);
        }
    }
}
