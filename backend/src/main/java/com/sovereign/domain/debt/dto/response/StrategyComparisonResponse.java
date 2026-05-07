package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record StrategyComparisonResponse(
    BigDecimal extraMonthlyPayment,
    BigDecimal totalDebt,
    StrategyResult avalanche,
    StrategyResult snowball,
    StrategyResult custom,
    String recommendation
) {}