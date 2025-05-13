package com.rtb.tenant.dto.Tenant_Insights.Request;

import com.rtb.tenant.dto.UpdateChannels;

import java.util.List;

public record UpdateCommunicationStatusDTO(
        List<UpdateChannels> updateChannels,
        InsightsFrontendDto insightsDataFrontend
) {
}
