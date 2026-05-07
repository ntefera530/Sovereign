package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AmortizationEntry(
    int monthNumber,
    LocalDate paymentDate,
    BigDecimal payment,
    BigDecimal principal,
    BigDecimal interest,
    BigDecimal remainingBalance
) {}