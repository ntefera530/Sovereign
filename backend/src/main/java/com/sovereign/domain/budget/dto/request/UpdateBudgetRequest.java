package com.sovereign.domain.budget.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateBudgetRequest(
    @NotBlank(message = "Budget name is required")
    String name,

    LocalDate endDate
) {}