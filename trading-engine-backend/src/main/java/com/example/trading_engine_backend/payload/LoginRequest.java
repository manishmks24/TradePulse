package com.example.trading_engine_backend.payload;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
