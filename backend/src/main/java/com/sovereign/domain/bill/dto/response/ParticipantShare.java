package com.sovereign.domain.bill.dto.response;

import java.math.BigDecimal;

public record ParticipantShare(
    String name,
    BigDecimal itemsTotal,
    BigDecimal tipShare,
    BigDecimal taxShare,
    BigDecimal total
) {}