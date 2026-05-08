package com.sovereign.domain.networth.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateAssetRequest(
    @NotNull(message = "Asset value is required")
    @Positive(message = "Asset value must be positive")
    BigDecimal value,

    @NotNull(message = "Valuation date is required")
    LocalDate valuationDate,

    String notes
) {}