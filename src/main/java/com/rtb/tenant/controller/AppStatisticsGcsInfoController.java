package com.rtb.tenant.controller;

import com.rtb.tenant.configuration.PlatformAdminGuard;
import com.rtb.tenant.dto.GcsAppRequestDto;
import com.rtb.tenant.exception.BadRequestException;
import com.rtb.tenant.service.AppStatisticsGcsInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tenants/app-statistics")
public class AppStatisticsGcsInfoController {
    private final AppStatisticsGcsInfoService appStatisticsGcsInfoService;
    public AppStatisticsGcsInfoController(
        AppStatisticsGcsInfoService appStatisticsGcsInfoService
    ) {
        this.appStatisticsGcsInfoService = appStatisticsGcsInfoService;
    }

    @PostMapping
    @PlatformAdminGuard
    public ResponseEntity<String> registerAppGcsDetails(
            @RequestBody GcsAppRequestDto request
    ) {
        try {
            return ResponseEntity.ok().body(appStatisticsGcsInfoService
                    .registerAppGcsDetails(request.getTenantId(), request.getGcsBucketName()));
        } catch (Exception e) {
            throw new BadRequestException("Something went wrong");
        }
    }

    @DeleteMapping
    @PlatformAdminGuard
    public ResponseEntity<String> deleteGcsDetailsForTenant(
            @RequestParam Long tenantId
    ) {
        return ResponseEntity.ok().body(appStatisticsGcsInfoService
                .deleteGcsDetailsForTenant(tenantId));
    }
}
