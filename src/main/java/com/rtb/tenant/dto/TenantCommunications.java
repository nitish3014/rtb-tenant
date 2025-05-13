package com.rtb.tenant.dto;

import java.util.List;

public record TenantCommunications(
    List<TenantCommunicationDto> communications
) {
}
