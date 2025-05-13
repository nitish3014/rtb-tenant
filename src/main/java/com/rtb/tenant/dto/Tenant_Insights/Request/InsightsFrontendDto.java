package com.rtb.tenant.dto.Tenant_Insights.Request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class InsightsFrontendDto {
    private String osVersion;
    private String ipAddress;
    private String location;
    private String timeZone;
    private String androidId;
    private String appleId;
    private String deviceBrand;
    private String deviceModel;
    private String appVersion;
    private String networkProvider;

    @JsonCreator
    public InsightsFrontendDto(
            @JsonProperty("osVersion") String osVersion,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("location") String location,
            @JsonProperty("timeZone") String timeZone,
            @JsonProperty("androidId") String androidId,
            @JsonProperty("appleId") String appleId,
            @JsonProperty("deviceBrand") String deviceBrand,
            @JsonProperty("deviceModel") String deviceModel,
            @JsonProperty("appVersion") String appVersion,
            @JsonProperty("networkProvider") String networkProvider
    ) {
        this.osVersion = osVersion;
        this.ipAddress = ipAddress;
        this.location = location;
        this.timeZone = timeZone;
        this.androidId = androidId;
        this.appleId = appleId;
        this.deviceBrand = deviceBrand;
        this.deviceModel = deviceModel;
        this.appVersion = appVersion;
        this.networkProvider = networkProvider;
    }

}

