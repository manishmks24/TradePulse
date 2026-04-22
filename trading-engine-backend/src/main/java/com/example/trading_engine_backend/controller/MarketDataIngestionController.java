package com.example.trading_engine_backend.controller;

import com.example.trading_engine_backend.payload.MarketTick;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MarketDataIngestionController {

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestLiveMarketData(@RequestBody List<MarketTick> ticks) {
        for (MarketTick tick : ticks) {
            messagingTemplate.convertAndSend("/topic/market/" + tick.getSymbol(), tick);
        }
        return ResponseEntity.ok().build();
    }
}
