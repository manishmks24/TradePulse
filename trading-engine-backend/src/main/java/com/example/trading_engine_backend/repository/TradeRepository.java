package com.example.trading_engine_backend.repository;

import com.example.trading_engine_backend.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByStockIdOrderByTimestampDesc(Long stockId);
    
    List<Trade> findByBuyerOrderUserIdOrSellerOrderUserIdOrderByTimestampDesc(Long buyerId, Long sellerId);
}
