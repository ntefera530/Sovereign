package com.sovereign.domain.account.dto.request;

import com.sovereign.common.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotBlank(message = "Account name is required")
    String name,

    @NotNull(message = "Account type is required")
    AccountType type,

    @NotNull(message = "Initial balance is required")
    BigDecimal initialBalance,

    @NotBlank(message = "Currency is required")
    String currency
) {}