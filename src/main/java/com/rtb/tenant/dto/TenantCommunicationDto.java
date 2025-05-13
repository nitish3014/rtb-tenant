package com.rtb.tenant.dto;

public record TenantCommunicationDto(
    String communicationChannel,
    String templateUrl,
    String category
) {
}
