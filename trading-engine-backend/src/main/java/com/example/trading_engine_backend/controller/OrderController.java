package com.example.trading_engine_backend.controller;

import com.example.trading_engine_backend.model.TradeOrder;
import com.example.trading_engine_backend.payload.OrderRequest;
import com.example.trading_engine_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // For local dev Angular
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<TradeOrder> placeOrder(@RequestBody OrderRequest request) {
        TradeOrder order = orderService.placeOrder(request);
        return ResponseEntity.ok(order);
    }
}
