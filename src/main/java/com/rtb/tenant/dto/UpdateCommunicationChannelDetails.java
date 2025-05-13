package com.rtb.tenant.dto;

public record UpdateCommunicationChannelDetails(
    Long tenantId,
    Long communicationId,
    String active,
    String templateUrl,
    String category
) {
}
