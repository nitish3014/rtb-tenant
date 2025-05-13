package com.rtb.tenant.dto.Tenant_Insights.Request;


import lombok.Builder;

@Builder
public record TenantInsightDto(
        Long communicationId,
        Long featureId,
        InsightsFrontendDto insightsDataFrontend

) {

}
