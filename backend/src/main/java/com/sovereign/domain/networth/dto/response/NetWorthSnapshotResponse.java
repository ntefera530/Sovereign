package com.sovereign.domain.networth.dto.response;

import com.sovereign.common.enums.NetWorthCalculationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record NetWorthSnapshotResponse(
    UUID id,
    NetWorthCalculationType calculationType,
    BigDecimal totalAssets,
    BigDecimal totalLiabilities,
    BigDecimal netWorth,
    LocalDateTime snapshotDate
) {}
