package com.sovereign.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    boolean emailVerified,
    LocalDateTime createdAt
) {}