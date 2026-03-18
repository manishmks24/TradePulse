package com.example.trading_engine_backend.engine;

import com.example.trading_engine_backend.model.AppUser;
import com.example.trading_engine_backend.model.Portfolio;
import com.example.trading_engine_backend.model.Stock;
import com.example.trading_engine_backend.model.Trade;
import com.example.trading_engine_backend.model.TradeOrder;
import com.example.trading_engine_backend.model.enums.OrderStatus;
import com.example.trading_engine_backend.repository.AppUserRepository;
import com.example.trading_engine_backend.repository.PortfolioRepository;
import com.example.trading_engine_backend.repository.TradeOrderRepository;
import com.example.trading_engine_backend.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeExecutionService {

    private final AppUserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final TradeOrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void executeTrade(Long buyerOrderId, Long sellerOrderId, BigDecimal matchPrice, int matchQuantity) {
        log.info("Executing trade: BuyerOrder={}, SellerOrder={}, Price={}, Qty={}", buyerOrderId, sellerOrderId, matchPrice, matchQuantity);

        TradeOrder buyerOrder = orderRepository.findById(buyerOrderId).orElseThrow();
        TradeOrder sellerOrder = orderRepository.findById(sellerOrderId).orElseThrow();

        // Lock users
        AppUser buyer = userRepository.findByIdForUpdate(buyerOrder.getUser().getId()).orElseThrow();
        AppUser seller = userRepository.findByIdForUpdate(sellerOrder.getUser().getId()).orElseThrow();

        Stock stock = buyerOrder.getStock();

        // Lock internal portfolios
        Portfolio buyerPortfolio = portfolioRepository.findByUserIdAndStockIdForUpdate(buyer.getId(), stock.getId())
                .orElseGet(() -> portfolioRepository.save(Portfolio.builder().user(buyer).stock(stock).quantity(0).build()));
        
        Portfolio sellerPortfolio = portfolioRepository.findByUserIdAndStockIdForUpdate(seller.getId(), stock.getId())
                .orElseThrow(() -> new IllegalStateException("Seller has no portfolio"));

        // Calculate trade value
        BigDecimal tradeValue = matchPrice.multiply(BigDecimal.valueOf(matchQuantity));

        // Note: Funds and Stocks were already locked/subtracted during Order Placement in OrderService.
        // We only need to ADD the assets to the receiving parties.
        seller.setCashBalance(seller.getCashBalance().add(tradeValue));
        buyerPortfolio.setQuantity(buyerPortfolio.getQuantity() + matchQuantity);

        userRepository.save(buyer);
        userRepository.save(seller);
        portfolioRepository.save(buyerPortfolio);
        portfolioRepository.save(sellerPortfolio);

        // Update orders
        buyerOrder.setRemainingQuantity(buyerOrder.getRemainingQuantity() - matchQuantity);
        sellerOrder.setRemainingQuantity(sellerOrder.getRemainingQuantity() - matchQuantity);

        buyerOrder.setStatus(buyerOrder.getRemainingQuantity() == 0 ? OrderStatus.FILLED : OrderStatus.PARTIAL);
        sellerOrder.setStatus(sellerOrder.getRemainingQuantity() == 0 ? OrderStatus.FILLED : OrderStatus.PARTIAL);

        orderRepository.save(buyerOrder);
        orderRepository.save(sellerOrder);

        // Record Trade
        Trade trade = Trade.builder()
                .buyerOrder(buyerOrder)
                .sellerOrder(sellerOrder)
                .stock(stock)
                .executionPrice(matchPrice)
                .quantity(matchQuantity)
                .timestamp(Instant.now())
                .build();
        tradeRepository.save(trade);

        // Broadcast to users (simple topic for Demo purposes since no Spring Security Principal)
        messagingTemplate.convertAndSend("/topic/trades/" + buyer.getId(), trade);
        messagingTemplate.convertAndSend("/topic/trades/" + seller.getId(), trade);

        log.info("Trade successfully executed and recorded. Trade ID: {}", trade.getId());
    }
}
