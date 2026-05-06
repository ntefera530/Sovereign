package com.sovereign.domain.account.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateAccountRequest(
    @NotBlank(message = "Account name is required")
    String name,

    boolean isActive
) {}