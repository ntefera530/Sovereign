package com.sovereign.domain.budget.dto.response;

import com.sovereign.common.enums.BudgetPeriodType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BudgetResponse(
    UUID id,
    String name,
    BudgetPeriodType periodType,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalLimit,
    BigDecimal totalSpent,
    BigDecimal totalRemaining,
    boolean isActive,
    List<BudgetCategoryResponse> categories,
    LocalDateTime createdAt
) {}