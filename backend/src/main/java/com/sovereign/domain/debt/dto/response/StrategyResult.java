package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record StrategyResult(
    String strategyName,
    String description,
    LocalDate payoffDate,
    int totalMonths,
    BigDecimal totalInterestPaid,
    BigDecimal totalAmountPaid,
    List<DebtPayoffOrder> payoffOrder
) {}