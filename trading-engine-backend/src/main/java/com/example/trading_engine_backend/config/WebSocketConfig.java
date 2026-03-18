package com.example.trading_engine_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Pure websocket endpoint
        registry.addEndpoint("/ws").setAllowedOrigins("http://localhost:4200", "http://localhost:4200/");
        // Fallback options available, allowed origins for local Angular app
        registry.addEndpoint("/ws-sockjs").setAllowedOrigins("http://localhost:4200", "http://localhost:4200/").withSockJS();
    }
}
