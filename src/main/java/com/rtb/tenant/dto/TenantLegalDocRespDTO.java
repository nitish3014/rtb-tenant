package com.rtb.tenant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantLegalDocRespDTO {
    private String privacyPolicyLink;
    private String termsConditionsLink;
    private String aboutUsLink;
}
