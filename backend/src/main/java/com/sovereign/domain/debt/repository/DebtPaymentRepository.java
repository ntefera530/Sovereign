package com.sovereign.domain.debt.repository;

import com.sovereign.domain.debt.entity.DebtPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, UUID> {
    List<DebtPayment> findByDebtId(UUID debtId);
    List<DebtPayment> findByDebtIdOrderByPaymentDateDesc(UUID debtId);
    List<DebtPayment> findByDebtIdAndPaymentDateBetween(UUID debtId, LocalDate start, LocalDate end);

    @Query("SELECT SUM(dp.amount) FROM DebtPayment dp WHERE dp.debt.id = :debtId")
    BigDecimal getTotalPaidByDebtId(@Param("debtId") UUID debtId);

    @Query("SELECT SUM(dp.extraAmount) FROM DebtPayment dp WHERE dp.debt.id = :debtId")
    BigDecimal getTotalExtraPaymentsByDebtId(@Param("debtId") UUID debtId);
}