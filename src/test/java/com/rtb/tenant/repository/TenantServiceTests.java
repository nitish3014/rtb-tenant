package com.rtb.core.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.tenant.dto.Tenant_Insights.Request.InsightsFrontendDto;
import com.rtb.core.entity.tenant.Tenant;
import com.rtb.tenant.exception.ResourceNotFoundException;
import com.rtb.tenant.service.InsightEventService;
import com.rtb.tenant.service.TenantService;
import com.rtb.tenant.utls.AuthenticationDetailUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Optional;

class TenantServiceTests {

  private TenantService tenantService;
  private TenantRepository tenantRepository;
  private FeaturePermissionRepository featurePermissionRepository;
  private PermissionRepository permissionRepository;
  private InsightEventService insightEventService;
  private AuthenticationDetailUtil authenticationDetailUtil;
  private WebClient.Builder webClientBuilder;

  @BeforeEach
  void setUp() {

    tenantRepository = mock(TenantRepository.class);
    featurePermissionRepository = mock(FeaturePermissionRepository.class);
    permissionRepository = mock(PermissionRepository.class);
    insightEventService = mock(InsightEventService.class);
    authenticationDetailUtil = mock(AuthenticationDetailUtil.class);


    webClientBuilder = mock(WebClient.Builder.class);
    WebClient webClient = mock(WebClient.class);

    when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
    when(webClientBuilder.build()).thenReturn(webClient);


    tenantService = new TenantService(
            new ObjectMapper(),
            tenantRepository,
            null,
            webClientBuilder,
            null,
            null,
            "http://fake-url",
            null,
            null,
            insightEventService,
            authenticationDetailUtil,
            featurePermissionRepository,
            permissionRepository
    );
  }

  @Test
  void testEnableTenant() {

    when(authenticationDetailUtil
            .getAuthenticationDetails("id")).thenReturn(123L);

    Tenant tenant = new Tenant();
    tenant.setId(1L);
    tenant.setEnabled(false);

    when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
    when(tenantRepository
            .save(any(Tenant.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    InsightsFrontendDto frontendDto = InsightsFrontendDto.builder()
            .osVersion("df")
            .appVersion("ds")
            .appleId("dd")
            .deviceBrand("df")
            .ipAddress("dfdf")
            .networkProvider("df")
            .timeZone("fdf")
            .androidId("fdf")
            .location("fdf")
            .deviceModel("fdf")
            .build();

    // Act: Call the method under test
    Tenant enabledTenant = tenantService.enableTenant(1L, frontendDto);

    // Assert: Verify behavior and state
    assertTrue(enabledTenant.isEnabled());
//    verify(insightEventService).sendEvents(
//            eq(InsightsEventId.ENABLE_TENANT_SUCCESS),
//            any(TenantInsightDto.class),
//            eq(InsightsMessages.ENABLE_TENANT_SUCCESS),
//            eq(true),
//            eq(200),
//            eq(0L),
//            eq(123L)
//    );
  }

  @Test
  void testEnableTenantNotFound() {

    when(tenantRepository.findById(anyLong())).thenReturn(Optional.empty());

    InsightsFrontendDto frontendDto = InsightsFrontendDto.builder()
            .osVersion("df")
            .appVersion("ds")
            .appleId("dd")
            .deviceBrand("df")
            .ipAddress("dfdf")
            .networkProvider("df")
            .timeZone("fdf")
            .androidId("fdf")
            .location("fdf")
            .deviceModel("fdf")
            .build();

    assertThrows(ResourceNotFoundException.class,
            () -> tenantService.enableTenant(1L, frontendDto));
  }
}
