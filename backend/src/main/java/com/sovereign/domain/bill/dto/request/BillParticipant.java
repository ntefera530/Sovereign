package com.sovereign.domain.bill.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record BillParticipant(
    @NotBlank(message = "Participant name is required")
    String name,

    // items this person ordered — empty means equal split
    List<BigDecimal> items
) {}