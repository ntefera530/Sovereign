package com.sovereign.domain.networth.dto.request;

import com.sovereign.common.enums.AssetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateAssetRequest(
    @NotBlank(message = "Asset name is required")
    String name,

    @NotNull(message = "Asset type is required")
    AssetType type,

    @NotNull(message = "Asset value is required")
    @Positive(message = "Asset value must be positive")
    BigDecimal value,

    @NotBlank(message = "Currency is required")
    String currency,

    @NotNull(message = "Valuation date is required")
    LocalDate valuationDate,

    String notes
) {}