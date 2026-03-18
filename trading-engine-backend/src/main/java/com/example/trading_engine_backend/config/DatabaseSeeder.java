package com.example.trading_engine_backend.config;

import com.example.trading_engine_backend.model.AppUser;
import com.example.trading_engine_backend.model.Portfolio;
import com.example.trading_engine_backend.model.Stock;
import com.example.trading_engine_backend.repository.AppUserRepository;
import com.example.trading_engine_backend.repository.PortfolioRepository;
import com.example.trading_engine_backend.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;

    @Override
    public void run(String... args) throws Exception {
        if (stockRepository.count() == 0) {
            Stock aapl = stockRepository.save(Stock.builder().symbol("AAPL").companyName("Apple Inc.").build());
            Stock goog = stockRepository.save(Stock.builder().symbol("GOOGL").companyName("Alphabet Inc.").build());
            Stock tsla = stockRepository.save(Stock.builder().symbol("TSLA").companyName("Tesla Inc.").build());

            AppUser alice = userRepository.save(AppUser.builder().username("alice").cashBalance(new BigDecimal("10000.00")).build());
            AppUser bob = userRepository.save(AppUser.builder().username("bob").cashBalance(new BigDecimal("15000.00")).build());

            portfolioRepository.save(Portfolio.builder().user(alice).stock(aapl).quantity(50).build());
            portfolioRepository.save(Portfolio.builder().user(alice).stock(tsla).quantity(10).build());
            
            portfolioRepository.save(Portfolio.builder().user(bob).stock(goog).quantity(20).build());
            portfolioRepository.save(Portfolio.builder().user(bob).stock(aapl).quantity(100).build());
        }
    }
}
