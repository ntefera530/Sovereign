package com.sovereign.domain.debt.dto.response;

import com.sovereign.common.enums.DebtType;
import com.sovereign.common.enums.PaymentFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DebtResponse(
    UUID id,
    String name,
    DebtType debtType,
    BigDecimal principal,
    BigDecimal currentBalance,
    BigDecimal interestRate,
    BigDecimal minimumPayment,
    PaymentFrequency paymentFrequency,
    LocalDate dueDate,
    boolean isPaidOff,
    BigDecimal dailyInterestCost,
    BigDecimal monthlyInterestCost,
    BigDecimal percentagePaidOff,
    LocalDateTime createdAt
) {}