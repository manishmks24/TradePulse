package com.example.trading_engine_backend.service;

import com.example.trading_engine_backend.model.AppUser;
import com.example.trading_engine_backend.payload.PortfolioDto;
import com.example.trading_engine_backend.repository.AppUserRepository;
import com.example.trading_engine_backend.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final AppUserRepository userRepository;

    public List<PortfolioDto> getUserPortfolio(Long userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(p -> PortfolioDto.builder()
                        .stockId(p.getStock().getId())
                        .symbol(p.getStock().getSymbol())
                        .companyName(p.getStock().getCompanyName())
                        .quantity(p.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    public BigDecimal getUserCashBalance(Long userId) {
        return userRepository.findById(userId)
                .map(AppUser::getCashBalance)
                .orElse(BigDecimal.ZERO);
    }
}
