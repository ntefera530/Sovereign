package com.sovereign.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateSettingsRequest(
    @NotBlank(message = "Currency is required")
    String currency,

    @NotBlank(message = "Locale is required")
    String locale,

    @NotBlank(message = "Timezone is required")
    String timezone,

    @NotBlank(message = "Theme is required")
    String theme,

    boolean notificationsEnabled
) {}