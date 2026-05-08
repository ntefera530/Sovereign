package com.sovereign.domain.networth.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record NetWorthHistoryResponse(
    List<NetWorthSnapshotResponse> snapshots,
    BigDecimal changeFromFirst,
    BigDecimal changeFromLast,
    BigDecimal highestNetWorth,
    BigDecimal lowestNetWorth,
    String trend
) {}