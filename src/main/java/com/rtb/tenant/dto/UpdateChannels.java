package com.rtb.tenant.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateChannels(
    @NotNull(message = "Communication Channel's Id cannot be null")
    Long id,

    @NotEmpty(message = "Channel cannot be empty")
    String channel,

    @NotEmpty(message = "Communication Channel's status cannot be empty")
    Boolean active
) {
}
