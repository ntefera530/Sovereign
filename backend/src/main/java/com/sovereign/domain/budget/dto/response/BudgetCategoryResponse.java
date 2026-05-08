package com.sovereign.domain.budget.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetCategoryResponse(
    UUID id,
    String name,
    BigDecimal limitAmount,
    BigDecimal spentAmount,
    BigDecimal remainingAmount,
    BigDecimal percentageUsed,
    boolean isOverBudget,
    boolean isApproachingLimit,
    LocalDateTime createdAt
) {}