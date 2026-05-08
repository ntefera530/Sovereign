package com.sovereign.domain.networth.dto.request;

import com.sovereign.common.enums.NetWorthCalculationType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record NetWorthRequest(
    @NotNull(message = "Calculation type is required")
    NetWorthCalculationType calculationType,

    // only used when calculationType = CUSTOM
    List<UUID> customAccountIds
) {}
