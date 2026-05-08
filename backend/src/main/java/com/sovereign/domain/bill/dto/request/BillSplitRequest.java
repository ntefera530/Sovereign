package com.sovereign.domain.bill.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record BillSplitRequest(
    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be positive")
    BigDecimal subtotal,

    @NotNull(message = "Tip percent is required")
    @PositiveOrZero(message = "Tip percent cannot be negative")
    BigDecimal tipPercent,

    @NotEmpty(message = "At least one participant is required")
    List<BillParticipant> participants,

    // optional tax percent
    @PositiveOrZero
    BigDecimal taxPercent
) {}