package com.example.trading_engine_backend.service;

import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeed.FeedResponse;
import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeed.Feed;
import com.upstox.marketdatafeederv3udapi.rpc.proto.MarketDataFeed.LTPC;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

@Service
public class UpstoxFeedService {

    private WebSocketClient webSocketClient;
    private final String UPSTOX_WS_URL = "wss://api.upstox.com/v2/feed/market-data-feed";

    // TODO: Replace with your actual Access Token generated from the Upstox API console
    private final String ACCESS_TOKEN = "YOUR_ACCESS_TOKEN";

    // Uncomment this to have it start automatically when the Spring app boots up
    // @PostConstruct
    public void connectToUpstox() {
        if ("YOUR_ACCESS_TOKEN".equals(ACCESS_TOKEN)) {
            System.out.println("Skipping Upstox WebSocket connection because access token is missing.");
            return;
        }

        try {
            webSocketClient = new WebSocketClient(new URI(UPSTOX_WS_URL)) {

                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("[Upstox] Connected to Real-Time Market Feed!");
                    subscribeToInstruments();
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        // Decode the binary protobuf payload into structured Java objects
                        FeedResponse response = FeedResponse.parseFrom(bytes.array());
                        
                        // Extract relevant stock ticks
                        for (Map.Entry<String, Feed> entry : response.getFeedsMap().entrySet()) {
                            String instrumentKey = entry.getKey();
                            Feed feed = entry.getValue();
                            
                            if (feed.hasFullFeed() && feed.getFullFeed().hasMarketFF()) {
                                LTPC ltpc = feed.getFullFeed().getMarketFF().getLtpc();
                                double lastPrice = ltpc.getLtp();
                                System.out.println("[Live Quote] " + instrumentKey + " -> ₹" + lastPrice);
                            }
                        }

                    } catch (Exception e) {
                        System.err.println("Failed to parse Upstox Protobuf response: " + e.getMessage());
                    }
                }

                @Override
                public void onMessage(String message) {
                    // Protocol Buffers are sent as bytes, this is usually empty for V3 feed
                    System.out.println("String message received: " + message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("[Upstox] Connection Closed. Code: " + code + ", Reason: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    System.err.println("[Upstox] WebSocket Error occurred:");
                    ex.printStackTrace();
                }
            };

            // Vital Setup: You must include the Bearer token and protocol headers
            webSocketClient.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
            webSocketClient.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void subscribeToInstruments() {
        // Send a subscription specific to Upstox JSON structure to begin receiving data
        // For example, an instrumentKey for Reliance is NSE_EQ|INE002A01018
        String payload = "{\"guid\":\"some-unique-id\",\"method\":\"sub\",\"data\":{\"instrumentKeys\":[\"NSE_EQ|INE002A01018\"],\"mode\":\"full\"}}";
        System.out.println("[Upstox] Subscribing to Reliance...");
        webSocketClient.send(payload.getBytes());
    }
}
