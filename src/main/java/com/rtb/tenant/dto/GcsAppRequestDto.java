package com.rtb.tenant.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GcsAppRequestDto {
    private String gcsBucketName;
    private Long tenantId;

}
