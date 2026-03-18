package com.example.trading_engine_backend.repository;

import com.example.trading_engine_backend.model.TradeOrder;
import com.example.trading_engine_backend.model.enums.OrderStatus;
import com.example.trading_engine_backend.model.enums.OrderType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {

    List<TradeOrder> findByUserIdOrderByTimestampDesc(Long userId);

    @Query("SELECT o FROM TradeOrder o WHERE o.stock.id = :stockId AND o.type = :type AND o.status IN (:statuses) ORDER BY " +
            "CASE WHEN o.type = com.example.trading_engine_backend.model.enums.OrderType.BID THEN o.price END DESC, " +
            "CASE WHEN o.type = com.example.trading_engine_backend.model.enums.OrderType.ASK THEN o.price END ASC, " +
            "o.timestamp ASC")
    List<TradeOrder> findActiveOrdersForMatching(@Param("stockId") Long stockId, 
                                                 @Param("type") OrderType type, 
                                                 @Param("statuses") List<OrderStatus> statuses, 
                                                 Pageable pageable);
}
