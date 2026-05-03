package com.sovereign.domain.debt.repository;

import com.sovereign.domain.debt.entity.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface DebtRepository extends JpaRepository<Debt, UUID> {
    List<Debt> findByUserId(UUID userId);
    List<Debt> findByUserIdAndIsPaidOffFalse(UUID userId);
    List<Debt> findByUserIdAndIsPaidOffTrue(UUID userId);

    @Query("SELECT SUM(d.currentBalance) FROM Debt d WHERE d.user.id = :userId AND d.isPaidOff = false")
    BigDecimal getTotalDebtByUserId(@Param("userId") UUID userId);
}