package com.sovereign.domain.budget.dto.request;

import com.sovereign.common.enums.BudgetPeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBudgetRequest(
    @NotBlank(message = "Budget name is required")
    String name,

    @NotNull(message = "Period type is required")
    BudgetPeriodType periodType,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    LocalDate endDate
) {}