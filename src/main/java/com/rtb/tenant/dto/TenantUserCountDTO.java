package com.rtb.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TenantUserCountDTO {
    private Long tenantId;
    private String longName;
    private String logo;
    private String email;
    private String shortName;
    private long userCount;
    private boolean isEnabled;
}
