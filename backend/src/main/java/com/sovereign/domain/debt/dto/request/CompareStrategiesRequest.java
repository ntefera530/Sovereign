package com.sovereign.domain.debt.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CompareStrategiesRequest(
    @NotNull(message = "Extra monthly payment is required")
    @PositiveOrZero
    BigDecimal extraMonthlyPayment,

    List<UUID> customOrder
) {}