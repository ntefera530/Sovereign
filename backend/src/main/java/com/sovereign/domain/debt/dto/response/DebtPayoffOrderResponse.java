package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record DebtPayoffOrderResponse(
    UUID debtId,
    String debtName,
    int payoffOrder,
    LocalDate payoffDate,
    BigDecimal totalInterestPaid
) {}