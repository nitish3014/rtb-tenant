package com.rtb.core.repository;

import com.rtb.core.entity.tenant.ConfigData;
import com.rtb.core.entity.tenant.Tenant;
import com.rtb.core.entity.tenant.TenantConfig;
import com.rtb.tenant.service.TenantConfigService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springdoc.api.OpenApiResourceNotFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TenantConfigServiceTests {

  @Mock
  private TenantRepository tenantRepository;

  @Mock
  private TenantConfigRepository tenantConfigRepository;

  @InjectMocks
  private TenantConfigService tenantConfigService;

  @Test
  public void configureTenantCreateNewConfig() {

    Long tenantId = 1L;
    ConfigData configData = new ConfigData();
    configData.setAtt1("Value1");
    configData.setAtt2("Value2");

    Tenant tenant = new Tenant();
    tenant.setId(tenantId);

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
    when(tenantConfigRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());
    when(tenantConfigRepository.save(any(TenantConfig.class))).thenAnswer(invocation ->
            invocation.getArgument(0));


    TenantConfig tenantConfig = tenantConfigService.configureTenant(tenantId, configData);


    assertThat(tenantConfig).isNotNull();
    assertThat(tenantConfig.getConfigData()).isNotNull();
    assertThat(tenantConfig.getConfigData().getAtt1()).isEqualTo("Value1");
    assertThat(tenantConfig.getConfigData().getAtt2()).isEqualTo("Value2");
    assertThat(tenantConfig.getTenant()).isEqualTo(tenant);

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantConfigRepository, times(1)).findByTenantId(tenantId);
    verify(tenantConfigRepository, times(1)).save(any(TenantConfig.class));
  }

  @Test
  public void configureTenantTenantNotFound() {

    Long tenantId = 1L;
    ConfigData configData = new ConfigData();

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(OpenApiResourceNotFoundException.class, () -> {
      tenantConfigService.configureTenant(tenantId, configData);
    });

    assertThat(exception.getMessage()).isEqualTo("Tenant not found with id: " + tenantId);

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantConfigRepository, times(0)).findByTenantId(tenantId);
    verify(tenantConfigRepository, times(0)).save(any(TenantConfig.class));
  }

  @Test
  public void configureTenantUpdateExistingConfig() {

    Long tenantId = 1L;
    ConfigData configData = new ConfigData();
    configData.setAtt1("NewValue1");
    configData.setAtt2("NewValue2");

    Tenant tenant = new Tenant();
    tenant.setId(tenantId);

    TenantConfig existingTenantConfig = new TenantConfig();
    ConfigData existingConfigData = new ConfigData();
    existingConfigData.setAtt1("OldValue1");
    existingConfigData.setAtt2("OldValue2");
    existingTenantConfig.setConfigData(existingConfigData);
    existingTenantConfig.setTenant(tenant);

    when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
    when(tenantConfigRepository.findByTenantId(tenantId)).
            thenReturn(Optional.of(existingTenantConfig));
    when(tenantConfigRepository.save(existingTenantConfig)).thenReturn(existingTenantConfig);


    TenantConfig tenantConfig = tenantConfigService.configureTenant(tenantId, configData);


    assertThat(tenantConfig).isNotNull();
    assertThat(tenantConfig.getConfigData()).isNotNull();
    assertThat(tenantConfig.getConfigData().getAtt1()).isEqualTo("NewValue1");
    assertThat(tenantConfig.getConfigData().getAtt2()).isEqualTo("NewValue2");
    assertThat(tenantConfig.getTenant()).isEqualTo(tenant);

    verify(tenantRepository, times(1)).findById(tenantId);
    verify(tenantConfigRepository, times(1)).findByTenantId(tenantId);
    verify(tenantConfigRepository, times(1)).save(existingTenantConfig);
  }

  @Test
  public void getAllTenantConfigs() {

    TenantConfig tenantConfig1 = new TenantConfig();
    TenantConfig tenantConfig2 = new TenantConfig();
    List<TenantConfig> tenantConfigs = Arrays.asList(tenantConfig1, tenantConfig2);

    when(tenantConfigRepository.findAll()).thenReturn(tenantConfigs);

    List<TenantConfig> result = tenantConfigService.getAllTenantConfigs();

    assertThat(result).isEqualTo(tenantConfigs);
    verify(tenantConfigRepository, times(1)).findAll();
  }
}
