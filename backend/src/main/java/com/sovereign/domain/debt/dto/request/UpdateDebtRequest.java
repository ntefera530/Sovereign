package com.sovereign.domain.debt.dto.request;

import com.sovereign.common.enums.PaymentFrequency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDebtRequest(
    @NotBlank(message = "Debt name is required")
    String name,

    @NotNull(message = "Current balance is required")
    @PositiveOrZero
    BigDecimal currentBalance,

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "1.0")
    BigDecimal interestRate,

    @NotNull(message = "Minimum payment is required")
    @Positive
    BigDecimal minimumPayment,

    @NotNull
    PaymentFrequency paymentFrequency,

    @NotNull
    LocalDate dueDate
) {}