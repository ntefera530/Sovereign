package com.sovereign.domain.budget.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BudgetSummaryResponse(
    BigDecimal totalLimit,
    BigDecimal totalSpent,
    BigDecimal totalRemaining,
    BigDecimal percentageUsed,
    int categoriesOverBudget,
    int categoriesApproachingLimit,
    List<BudgetCategoryResponse> categories,
    String insight
) {}