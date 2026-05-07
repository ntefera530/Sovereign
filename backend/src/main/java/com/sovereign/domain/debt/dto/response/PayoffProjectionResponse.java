package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PayoffProjectionResponse(
    UUID debtId,
    String debtName,
    BigDecimal currentBalance,
    BigDecimal interestRate,
    BigDecimal monthlyPayment,
    LocalDate payoffDate,
    int monthsToPayoff,
    BigDecimal totalInterestPaid,
    BigDecimal totalAmountPaid,
    BigDecimal interestToBalanceRatio,
    List<AmortizationEntry> schedule
) {}