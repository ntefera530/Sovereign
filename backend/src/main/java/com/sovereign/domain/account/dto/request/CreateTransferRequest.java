package com.sovereign.domain.account.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateTransferRequest(
    @NotNull(message = "From account is required")
    UUID fromAccountId,

    @NotNull(message = "To account is required")
    UUID toAccountId,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    String note,

    @NotNull(message = "Transfer date is required")
    LocalDate transferDate
) {}