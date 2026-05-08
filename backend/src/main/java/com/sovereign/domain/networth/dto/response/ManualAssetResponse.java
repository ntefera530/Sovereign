package com.sovereign.domain.networth.dto.response;

import com.sovereign.common.enums.AssetType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ManualAssetResponse(
    UUID id,
    String name,
    AssetType type,
    BigDecimal value,
    String currency,
    LocalDate valuationDate,
    String notes,
    LocalDateTime createdAt
) {}
