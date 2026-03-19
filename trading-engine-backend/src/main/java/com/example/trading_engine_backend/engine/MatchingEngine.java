package com.example.trading_engine_backend.engine;

import com.example.trading_engine_backend.model.Stock;
import com.example.trading_engine_backend.model.TradeOrder;
import com.example.trading_engine_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEngine {

    private final StockRepository stockRepository;
    private final TradeExecutionService tradeExecutionService;
    
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    // One order book per stock
    private final ConcurrentHashMap<Long, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final ExecutorService matchingExecutor = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        stockRepository.findAll().forEach(stock -> {
            orderBooks.put(stock.getId(), new OrderBook(stock.getId()));
        });
    }

    public void submitOrder(TradeOrder order) {
        OrderBook book = orderBooks.computeIfAbsent(order.getStock().getId(), OrderBook::new);
        book.addOrder(order);
        
        broadcastOrderBook(book);

        // Asynchronously match to avoid blocking the caller submitting the order
        matchingExecutor.submit(() -> matchOrders(book));
    }

    private void broadcastOrderBook(OrderBook book) {
        Stock stock = stockRepository.findById(book.getStockId()).orElse(null);
        if (stock != null) {
            messagingTemplate.convertAndSend("/topic/orderbook/" + stock.getSymbol(), book.getSnapshot(stock.getSymbol()));
        }
    }

    private void matchOrders(OrderBook book) {
        // Synchronized to ensure sequential matching for a single stock's order book
        synchronized (book) {
            boolean matchesMade = false;
            while (true) {
                TradeOrder bestBid = book.peekBestBid();
                TradeOrder bestAsk = book.peekBestAsk();

                if (bestBid == null || bestAsk == null) {
                    break; // Missing one side of the book
                }

                if (bestBid.getPrice().compareTo(bestAsk.getPrice()) >= 0) {
                    // Match found! Highest bid is at least lowest ask
                    int matchQuantity = Math.min(bestBid.getRemainingQuantity(), bestAsk.getRemainingQuantity());
                    
                    // The execution price is the price of the order that rested in the book first
                    BigDecimal matchPrice = bestBid.getTimestamp().isBefore(bestAsk.getTimestamp()) ? 
                            bestBid.getPrice() : bestAsk.getPrice();

                    try {
                        // Execute transaction atomically in DB
                        tradeExecutionService.executeTrade(bestBid.getId(), bestAsk.getId(), matchPrice, matchQuantity);
                        
                        // Update remaining quantities in memory for the order book
                        bestBid.setRemainingQuantity(bestBid.getRemainingQuantity() - matchQuantity);
                        bestAsk.setRemainingQuantity(bestAsk.getRemainingQuantity() - matchQuantity);

                        if (bestBid.getRemainingQuantity() == 0) {
                            book.removeOrder(bestBid);
                        }
                        if (bestAsk.getRemainingQuantity() == 0) {
                            book.removeOrder(bestAsk);
                        }
                        matchesMade = true;

                    } catch (Exception e) {
                        log.error("Failed to execute trade", e);
                        // Stop matching this pair rather than retrying infinitely
                        break;
                    }
                } else {
                    // Highest bid is lower than lowest ask -> no more matches possible
                    break;
                }
            }
            if (matchesMade) {
                broadcastOrderBook(book);
            }
        }
    }
}
