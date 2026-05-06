package com.sovereign.domain.user.dto.response;

import java.util.UUID;

public record UserSettingsResponse(
    UUID id,
    String currency,
    String locale,
    String timezone,
    String theme,
    boolean notificationsEnabled
) {}