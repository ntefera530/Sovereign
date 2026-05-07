package com.sovereign.domain.debt.dto.request;

import com.sovereign.common.enums.DebtType;
import com.sovereign.common.enums.PaymentFrequency;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDebtRequest(
    @NotBlank(message = "Debt name is required")
    String name,

    @NotNull(message = "Debt type is required")
    DebtType debtType,

    @NotNull(message = "Principal is required")
    @Positive(message = "Principal must be positive")
    BigDecimal principal,

    @NotNull(message = "Current balance is required")
    @PositiveOrZero(message = "Current balance cannot be negative")
    BigDecimal currentBalance,

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    @DecimalMax(value = "1.0", message = "Interest rate must be a decimal e.g. 0.2499 for 24.99%")
    BigDecimal interestRate,

    @NotNull(message = "Minimum payment is required")
    @Positive(message = "Minimum payment must be positive")
    BigDecimal minimumPayment,

    @NotNull(message = "Payment frequency is required")
    PaymentFrequency paymentFrequency,

    @NotNull(message = "Due date is required")
    LocalDate dueDate
) {}