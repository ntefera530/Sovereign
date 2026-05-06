package com.sovereign.domain.account.dto.request;

import java.util.UUID;

public record UpdateTransactionRequest(
    String description,
    UUID budgetCategoryId
) {}