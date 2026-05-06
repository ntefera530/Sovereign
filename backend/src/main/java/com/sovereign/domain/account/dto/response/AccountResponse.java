package com.sovereign.domain.account.dto.response;

import com.sovereign.common.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    String name,
    AccountType type,
    BigDecimal balance,
    String currency,
    boolean isActive,
    LocalDateTime createdAt
) {}