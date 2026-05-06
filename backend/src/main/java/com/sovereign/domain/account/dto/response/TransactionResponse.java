package com.sovereign.domain.account.dto.response;

import com.sovereign.common.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID accountId,
    BigDecimal amount,
    TransactionType type,
    String description,
    LocalDate transactionDate,
    UUID budgetCategoryId,
    LocalDateTime createdAt
) {}