package com.sovereign.domain.networth.dto.response;

import com.sovereign.common.enums.NetWorthCalculationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record NetWorthResponse(
    NetWorthCalculationType calculationType,
    BigDecimal totalAssets,
    BigDecimal totalLiabilities,
    BigDecimal netWorth,
    BigDecimal liquidAssets,
    BigDecimal cashOnHand,
    List<AssetBreakdown> assetBreakdown,
    List<AssetBreakdown> liabilityBreakdown,
    LocalDateTime calculatedAt
) {}
