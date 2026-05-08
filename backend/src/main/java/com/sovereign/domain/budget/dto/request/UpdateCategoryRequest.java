package com.sovereign.domain.budget.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateCategoryRequest(
    @NotBlank(message = "Category name is required")
    String name,

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit amount must be positive")
    BigDecimal limitAmount
) {}