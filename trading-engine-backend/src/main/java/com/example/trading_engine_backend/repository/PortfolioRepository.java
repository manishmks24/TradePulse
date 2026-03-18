package com.example.trading_engine_backend.repository;

import com.example.trading_engine_backend.model.Portfolio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUserId(Long userId);

    Optional<Portfolio> findByUserIdAndStockId(Long userId, Long stockId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId AND p.stock.id = :stockId")
    Optional<Portfolio> findByUserIdAndStockIdForUpdate(@Param("userId") Long userId, @Param("stockId") Long stockId);
}
