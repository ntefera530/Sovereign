package com.sovereign.domain.networth.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AssetBreakdown(
    UUID sourceId,
    String sourceType,
    String name,
    BigDecimal value
) {}
