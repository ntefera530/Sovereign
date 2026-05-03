package com.sovereign.domain.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String firstName,
    String lastName,
    boolean emailVerified,
    LocalDateTime createdAt
) {}