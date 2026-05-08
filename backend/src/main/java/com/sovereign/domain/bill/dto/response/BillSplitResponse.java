package com.sovereign.domain.bill.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record BillSplitResponse(
    BigDecimal subtotal,
    BigDecimal tipPercent,
    BigDecimal tipAmount,
    BigDecimal taxPercent,
    BigDecimal taxAmount,
    BigDecimal totalBill,
    List<ParticipantShare> shares
) {}