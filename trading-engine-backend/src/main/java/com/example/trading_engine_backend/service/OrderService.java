package com.example.trading_engine_backend.service;

import com.example.trading_engine_backend.engine.MatchingEngine;
import com.example.trading_engine_backend.model.AppUser;
import com.example.trading_engine_backend.model.Portfolio;
import com.example.trading_engine_backend.model.Stock;
import com.example.trading_engine_backend.model.TradeOrder;
import com.example.trading_engine_backend.model.enums.OrderStatus;
import com.example.trading_engine_backend.model.enums.OrderType;
import com.example.trading_engine_backend.payload.OrderRequest;
import com.example.trading_engine_backend.repository.AppUserRepository;
import com.example.trading_engine_backend.repository.PortfolioRepository;
import com.example.trading_engine_backend.repository.StockRepository;
import com.example.trading_engine_backend.repository.TradeOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final TradeOrderRepository orderRepository;
    private final AppUserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final MatchingEngine matchingEngine;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TradeOrder placeOrder(OrderRequest request) {
        
        AppUser user = userRepository.findByIdForUpdate(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new IllegalArgumentException("Stock not found"));

        // Lock funds/assets
        if (request.getType() == OrderType.BID) {
            BigDecimal totalCost = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            if (user.getCashBalance().compareTo(totalCost) < 0) {
                throw new IllegalStateException("Insufficient funds");
            }
            user.setCashBalance(user.getCashBalance().subtract(totalCost));
            userRepository.save(user);
        } else {
            // It's an ASK
            Portfolio portfolio = portfolioRepository.findByUserIdAndStockIdForUpdate(user.getId(), stock.getId())
                    .orElseThrow(() -> new IllegalStateException("No portfolio for stock"));
            
            if (portfolio.getQuantity() < request.getQuantity()) {
                throw new IllegalStateException("Insufficient stock quantity");
            }
            portfolio.setQuantity(portfolio.getQuantity() - request.getQuantity());
            portfolioRepository.save(portfolio);
        }

        TradeOrder order = TradeOrder.builder()
                .user(user)
                .stock(stock)
                .type(request.getType())
                .status(OrderStatus.OPEN)
                .price(request.getPrice())
                .initialQuantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .timestamp(Instant.now())
                .build();

        TradeOrder savedOrder = orderRepository.save(order);

        // Submit to matching engine
        matchingEngine.submitOrder(savedOrder);

        return savedOrder;
    }
}
