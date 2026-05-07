package com.sovereign.domain.debt.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SimulatePayoffRequest(
    @NotNull(message = "Debt ID is required")
    java.util.UUID debtId,

    @NotNull(message = "Extra monthly payment is required")
    @PositiveOrZero
    BigDecimal extraMonthlyPayment
) {}