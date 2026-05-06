package com.sovereign.domain.account.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AccountSummaryResponse(
    BigDecimal totalBalance,
    int totalAccounts,
    List<AccountResponse> accounts
) {}