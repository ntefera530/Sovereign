package com.sovereign.domain.plaid.dto.response;

import com.sovereign.common.enums.SyncStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlaidItemResponse(
    UUID id,
    String institutionName,
    SyncStatus syncStatus,
    LocalDateTime lastSyncedAt,
    int accountCount
) {}