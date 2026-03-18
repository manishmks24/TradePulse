package com.example.trading_engine_backend.engine;

import com.example.trading_engine_backend.model.TradeOrder;

import java.util.Comparator;
import java.util.PriorityQueue;

public class OrderBook {

    private final Long stockId;

    // Highest price first, earliest timestamp first
    private final PriorityQueue<TradeOrder> bids;

    // Lowest price first, earliest timestamp first
    private final PriorityQueue<TradeOrder> asks;

    public OrderBook(Long stockId) {
        this.stockId = stockId;
        this.bids = new PriorityQueue<>(Comparator
                .comparing(TradeOrder::getPrice).reversed()
                .thenComparing(TradeOrder::getTimestamp));
        this.asks = new PriorityQueue<>(Comparator
                .comparing(TradeOrder::getPrice)
                .thenComparing(TradeOrder::getTimestamp));
    }

    public synchronized void addOrder(TradeOrder order) {
        switch (order.getType()) {
            case BID -> bids.offer(order);
            case ASK -> asks.offer(order);
        }
    }

    public synchronized TradeOrder peekBestBid() {
        return bids.peek();
    }

    public synchronized TradeOrder peekBestAsk() {
        return asks.peek();
    }

    public synchronized void removeOrder(TradeOrder order) {
        bids.remove(order);
        asks.remove(order);
    }
}
