package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record DebtPaymentResponse(
    UUID id,
    UUID debtId,
    BigDecimal amount,
    BigDecimal extraAmount,
    LocalDate paymentDate,
    String note,
    LocalDateTime createdAt
) {}