package com.sovereign.domain.debt.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record SimulationResponse(
    UUID debtId,
    String debtName,

    // baseline — minimum payments only
    LocalDate baselinePayoffDate,
    int baselineMonths,
    BigDecimal baselineTotalInterest,

    // with extra payment
    LocalDate simulatedPayoffDate,
    int simulatedMonths,
    BigDecimal simulatedTotalInterest,

    // the difference — this is what motivates people
    int monthsSaved,
    BigDecimal interestSaved,
    BigDecimal extraMonthlyPayment,

    List<AmortizationEntry> simulatedSchedule
) {}