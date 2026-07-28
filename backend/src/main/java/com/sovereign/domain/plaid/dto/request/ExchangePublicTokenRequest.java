package com.sovereign.domain.plaid.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ExchangePublicTokenRequest(@NotBlank String publicToken) {}