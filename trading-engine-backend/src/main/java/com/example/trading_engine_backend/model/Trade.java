package com.example.trading_engine_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_order_id", nullable = false)
    private TradeOrder buyerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_order_id", nullable = false)
    private TradeOrder sellerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal executionPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Instant timestamp;

}
