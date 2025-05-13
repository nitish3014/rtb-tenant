package com.rtb.tenant.service;

import com.rtb.core.entity.tenant.ConfigData;
import com.rtb.core.entity.tenant.Tenant;
import com.rtb.core.entity.tenant.TenantConfig;
import com.rtb.core.repository.TenantConfigRepository;
import com.rtb.core.repository.TenantRepository;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TenantConfigService {
  @Autowired
  private TenantRepository tenantRepository;

  @Autowired
  private TenantConfigRepository tenantConfigRepository;

  public TenantConfig configureTenant(Long tenantId, ConfigData configData) {
    Tenant tenant = tenantRepository.findById(tenantId)
      .orElseThrow(
        () ->
          new OpenApiResourceNotFoundException("Tenant not found with id: " + tenantId)
      );

    TenantConfig existingTenantConfig = tenantConfigRepository
      .findByTenantId(tenantId)
      .orElse(new TenantConfig());

    // Create a new ConfigData object if it doesn't exist
    ConfigData existingConfigData = existingTenantConfig.getConfigData();
    if (existingConfigData == null) {
      existingConfigData = new ConfigData();
      existingTenantConfig.setConfigData(existingConfigData);
    }

    // Update the attributes based on the provided configData
    if (configData.getAtt1() != null) {
      existingConfigData.setAtt1(configData.getAtt1());
    }

    if (configData.getAtt2() != null) {
      existingConfigData.setAtt2(configData.getAtt2());
    }

    // Update the configData in the TenantConfig entity
    existingTenantConfig.setConfigData(existingConfigData);
    existingTenantConfig.setTenant(tenant);

    return tenantConfigRepository.save(existingTenantConfig);
  }

  public List<TenantConfig> getAllTenantConfigs() {
    return tenantConfigRepository.findAll();
  }
}
