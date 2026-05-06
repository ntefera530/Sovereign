package com.sovereign.domain.account.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
    UUID id,
    UUID debitTransactionId,
    String fromAccountName,
    UUID creditTransactionId,
    String toAccountName,
    BigDecimal amount,
    boolean autoDetected,
    boolean userConfirmed,
    LocalDateTime createdAt
) {}