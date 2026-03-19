package com.example.trading_engine_backend.payload;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
}
