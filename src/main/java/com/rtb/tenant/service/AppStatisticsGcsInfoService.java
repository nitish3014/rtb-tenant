package com.rtb.tenant.service;

import com.rtb.core.entity.tenant.AppStatisticsGcsInfo;
import com.rtb.tenant.exception.BadRequestException;
import com.rtb.core.repository.AppStatisticsGcsInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AppStatisticsGcsInfoService {
    private final AppStatisticsGcsInfoRepository appStatisticsGcsInfoRepository;
    public AppStatisticsGcsInfoService(
        AppStatisticsGcsInfoRepository appStatisticsGcsInfoRepository
    ) {
        this.appStatisticsGcsInfoRepository = appStatisticsGcsInfoRepository;
    }

    public String registerAppGcsDetails(Long tenantId, String bucketName) {
        if (appStatisticsGcsInfoRepository.findByTenantId(tenantId).isPresent()) {
            throw new BadRequestException("Tenant already exists with id: " + tenantId);
        }
        if (appStatisticsGcsInfoRepository.findByGcsBucketName(bucketName).isPresent()) {
            throw new BadRequestException("Bucket already exists with name: " + bucketName);
        }

        AppStatisticsGcsInfo appStatisticsGcsInfo = new AppStatisticsGcsInfo();
        appStatisticsGcsInfo.setTenantId(tenantId);
        appStatisticsGcsInfo.setGcsBucketName(bucketName);
        appStatisticsGcsInfoRepository.save(appStatisticsGcsInfo);
        return "App Statistics details created for tenant id: " + tenantId;
    }

    @Transactional
    public String deleteGcsDetailsForTenant(Long tenantId) {
        appStatisticsGcsInfoRepository.deleteByTenantId(tenantId);
        return String.format(
                "Tenant with id %s Gcs details for statistics deleted successfully",
                tenantId);
    }
}
