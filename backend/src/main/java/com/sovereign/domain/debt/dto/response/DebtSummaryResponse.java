package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DebtSummaryResponse(
    BigDecimal totalDebt,
    BigDecimal totalMinimumPayments,
    BigDecimal totalMonthlyInterest,
    BigDecimal totalDailyInterest,
    LocalDate estimatedDebtFreeDate,
    int monthsToDebtFree,
    BigDecimal totalInterestIfMinimumsOnly,
    List<DebtResponse> debts
) {}