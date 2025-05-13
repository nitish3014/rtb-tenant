package com.rtb.tenant.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.core.entity.tenant.ConfigData;
import com.rtb.core.entity.tenant.Tenant;
import com.rtb.core.entity.tenant.TenantCommunicationChannels;
import com.rtb.core.entity.tenant.TenantConfig;
import com.rtb.tenant.configuration.PlatformAdminGuard;
import com.rtb.tenant.configuration.TenantAccessGuard;
import com.rtb.tenant.dto.TenantUserCountDTO;
import com.rtb.tenant.dto.UpdateChannels;
import com.rtb.tenant.dto.TenantCommunications;
import com.rtb.tenant.dto.UpdateList;
import com.rtb.tenant.dto.TenantLegalDocRespDTO;
import com.rtb.tenant.dto.FeatureDTO;
import com.rtb.tenant.dto.FeatureResponseDto;
import com.rtb.tenant.dto.Tenant_Insights.Request.InsightsFrontendDto;
import com.rtb.tenant.service.S3FileService;
import com.rtb.tenant.service.TenantConfigService;
import com.rtb.tenant.service.TenantService;
import com.rtb.tenant.utls.AuthenticationDetailUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Set;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestHeader;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("api/v1/tenants")
public class TenantController extends BaseController {

  private final TenantService tenantService;
  private final TenantConfigService tenantConfigService;
  private final S3FileService s3FileService;
  private final ObjectMapper objectMapper;
  private final AuthenticationDetailUtil authenticationDetailUtil;
  public TenantController(TenantService tenantService,
                          TenantConfigService tenantConfigService,
                          S3FileService s3FileService,
                          ObjectMapper objectMapper,
                          AuthenticationDetailUtil authenticationDetailUtil) {
    this.tenantService = tenantService;
    this.tenantConfigService = tenantConfigService;
    this.s3FileService = s3FileService;
    this.objectMapper = objectMapper;
    this.authenticationDetailUtil = authenticationDetailUtil;
  }

  @PostMapping //platform admin
  @PreAuthorize("hasAuthority('tenant_create')")
  @PlatformAdminGuard
  public ResponseEntity<Tenant> createTenant(
          @RequestBody Tenant tenant,
          @RequestHeader("X-Insights-Data") String insightsDataJson) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      Tenant createdTenant = tenantService
              .createTenant(tenant, insightsData);
      return new ResponseEntity<>(createdTenant, HttpStatus.CREATED);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(
              HttpStatus.BAD_REQUEST).build();
    }
  }

  @PatchMapping("/{tenantId}/enable") // platform admin
  @PreAuthorize("hasAuthority('tenant_update')")
  @PlatformAdminGuard
  public ResponseEntity<Tenant> enableTenant(
          @PathVariable Long tenantId,
          @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      Tenant tenant = tenantService.enableTenant(
              tenantId, insightsData);
      return new ResponseEntity<>(tenant, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(
              HttpStatus.BAD_REQUEST).build();
    }


  }

  @PatchMapping("/{tenantId}/disable") // platform admin
  @PreAuthorize("hasAuthority('tenant_update')")
  @PlatformAdminGuard
  public ResponseEntity<Tenant> disableTenant(
          @PathVariable Long tenantId,
          @RequestHeader("X-Insights-Data") String insightsDataJson) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      Tenant tenant = tenantService.disableTenant(tenantId, insightsData);
      return new ResponseEntity<>(tenant, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(
              HttpStatus.BAD_REQUEST).build();
    }

  }

  @PostMapping("/{tenantId}/configure") //Not using
  @PreAuthorize("hasAuthority('tenant_create')")
  public ResponseEntity<TenantConfig> configureTenant(
    @PathVariable Long tenantId,
    @RequestBody ConfigData configData
  ) {
    TenantConfig tenantConfig = tenantConfigService
            .configureTenant(tenantId, configData);
    if (tenantConfig != null) {
      return ResponseEntity.ok(tenantConfig);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping //Not using
  @PreAuthorize("hasAuthority('tenant_read')")
  public ResponseEntity<List<TenantConfig>> getAllTenantConfigs() {
    List<TenantConfig> tenantConfigs = tenantConfigService.getAllTenantConfigs();
    return ResponseEntity.ok(tenantConfigs);
  }


  @GetMapping("/all") //Platform admin
  @PreAuthorize("hasAuthority('tenant_read')")
  @PlatformAdminGuard
  public ResponseEntity<
          List<TenantUserCountDTO>
          > getAllTenants(@RequestHeader("X-Insights-Data")
                                     String insightsDataJson) {
    try {
      InsightsFrontendDto insightsData
              = objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class);
      List<TenantUserCountDTO> tenants =
              tenantService.getTenants(insightsData);
      return ResponseEntity.ok(tenants);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }


  @GetMapping("/tenants/check/{tenantId}") //Not used
  @PreAuthorize("hasAuthority('tenant_read')")
  public ResponseEntity<Boolean> checkTenant(
    @PathVariable("tenantId") Long id
  ) {
    return ResponseEntity.ok(
      tenantService.checkTenants(id)
    );
  }

  @GetMapping("/{tenantId}") // Platform and Tenant admin
  @PreAuthorize("hasAuthority('tenant_read')")
  @TenantAccessGuard
  public ResponseEntity<Tenant> gettenantid(
          @PathVariable Long tenantId,
          @RequestHeader("X-Insights-Data") String insightsDataJson) {
    try {
      InsightsFrontendDto insightsData =
              objectMapper.readValue(insightsDataJson, InsightsFrontendDto.class);
      Tenant tenant = tenantService.getTenantId(tenantId, insightsData);
      return new ResponseEntity<>(tenant, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PutMapping("/{tenantId}") // Platform and Tenant Admin
  @PreAuthorize("hasAuthority('tenant_update')")
  @TenantAccessGuard
  public ResponseEntity<Tenant> updateTenant(
    @PathVariable Long tenantId,
    @RequestBody Tenant tenant,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData =
              objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class
              );
      Tenant updatedTenant = tenantService
              .updateTenant(tenantId, tenant, insightsData);
      return ResponseEntity.ok(updatedTenant);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("{tenantId}/upload-logo") // Platform and Tenant Admin
  @PreAuthorize("hasAuthority('tenant_create')")
  @TenantAccessGuard
  public ResponseEntity<String> uploadTenantLogo(
    @PathVariable("tenantId") Long tenantID,
    @RequestPart("file") MultipartFile file,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      // check for file type
        if (!file.getContentType().equals("image/png")
                && !file.getContentType().equals("image/jpeg")) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Please upload a PNG or JPEG file.");
        }

      InsightsFrontendDto insightsData =
              objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class
              );
      String logoUrl = s3FileService
              .uploadFileAndSaveUrl(tenantID, file, insightsData);
      return ResponseEntity.ok(logoUrl);
    } catch (IOException e) {
      throw new ResponseStatusException(
              HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to upload file", e);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    } catch (RuntimeException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @PostMapping("{tenantId}/upload-template") // Platform and Tenant Admin
  @PreAuthorize("hasAuthority('tenant_create')")
  @TenantAccessGuard
  public ResponseEntity<String> uploadTenantTemplate(
    @PathVariable(name = "tenantId") Long tenantId,
    @RequestPart(name = "file") MultipartFile file,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {

      if (!file.getContentType().equals("text/plain")) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid file type. Please upload a TXT file.");
      }

      InsightsFrontendDto insightsData =
              objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class
              );
      String logoUrl = s3FileService.uploadTemplate(
              tenantId, file, insightsData);
      return ResponseEntity.ok(logoUrl);
    } catch (IOException e) {
      throw new ResponseStatusException(
              HttpStatus.INTERNAL_SERVER_ERROR,
              "Failed to upload file", e);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(
              HttpStatus.NOT_FOUND, e.getMessage());
    } catch (RuntimeException e) {
      throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  @GetMapping("{tenantId}/communication-channels") // Platform and Tenant Admin
  @PreAuthorize("hasAuthority('tenant_read')")
  @TenantAccessGuard
  public ResponseEntity<
          Set<TenantCommunicationChannels>
          > getAllChannels(
      @PathVariable(name = "tenantId") Long tenantId
  ) {
      Set<TenantCommunicationChannels> channels =
              tenantService.getAllChannels(tenantId);
      return ResponseEntity.ok(channels);
    }

  @PutMapping("{tenantId}/communication-channel/{communicationId}")
  @PreAuthorize("hasAuthority('tenant_update')")
  @TenantAccessGuard
  public ResponseEntity<String> updateChannel(
          @PathVariable(name = "tenantId") Long tenantId,
          @RequestBody List<UpdateChannels> updateChannels,
          @RequestHeader("X-Insights-Data") String insightsDataJson
          ) {
    try {
      InsightsFrontendDto insightsData =
              objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class
              );
      tenantService.updateChannels(updateChannels, insightsData);
      return ResponseEntity.ok("Status has been updated!");
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

  }

  @GetMapping(value = "{tenantId}/communication/categories") //Not using
  @PreAuthorize("hasAuthority('tenant_read')")
  @TenantAccessGuard
  public ResponseEntity<List<String>> getCommunicationCategories(
    @PathVariable(name = "tenantId") Long tenantId
  ) {
    List<String> categories = tenantService.getCommunicationCategories();
    return ResponseEntity.ok(categories);
  }

  // works
  @PostMapping(value = "{tenantId}/communication") // Platform admin, doubt
  @PreAuthorize("hasAuthority('tenant_create')")
  @TenantAccessGuard
  public ResponseEntity<Tenant> addCommunicationChannel(
    @PathVariable(name = "tenantId") Long tenantId,
    @RequestBody TenantCommunications tenantCommunications,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData =
              objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class
              );
      Tenant response = tenantService
              .addCommunication(tenantId, tenantCommunications, insightsData);
      return ResponseEntity.status(HttpStatus.OK).body(response);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
  }

  // works
  @PutMapping(
          value = "{tenantId}/communication/{communicationId}"
  )
  @PreAuthorize("hasAuthority('tenant_update')")
  @TenantAccessGuard
  public ResponseEntity<String> updateCommunicationChannel(
    @RequestBody UpdateList dto,
    @PathVariable(name = "tenantId") Long tenantId,
    @PathVariable(name = "communicationId") Long communicationId,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData =
              objectMapper.readValue(
                      insightsDataJson, InsightsFrontendDto.class
              );
      tenantService.updateCommunicationDetails(dto, tenantId, communicationId, insightsData);
      String responseBody = "Updated the changes";
      return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

  }

  // works
  @DeleteMapping(value = "{tenantId}/communication/{communicationId}") // Platform Admin
  @PreAuthorize("hasAuthority('tenant_delete')")
  @PlatformAdminGuard
  public ResponseEntity<String> deleteCommunicationChannel(
    @PathVariable(name = "tenantId") Long tenantId,
    @PathVariable(name = "communicationId") Long communicationId,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      tenantService.deleteCommunications(tenantId, communicationId, insightsData);
      String responseBody = "Communication has been deleted";
      return ResponseEntity.ok(responseBody);
    } catch (JsonProcessingException e) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST).build();
    }
  }

  @PostMapping("{tenantId}/upload-csv") // Platform and tenant admin
  @PreAuthorize("hasAuthority('tenant_create')")
  @TenantAccessGuard
  public ResponseEntity<String> uploadCSV(
    @PathVariable(name = "tenantId") Long tenantId,
    @RequestPart(name = "file") MultipartFile file,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      if (!file.getContentType().equals("text/csv")) {
          return ResponseEntity
                  .status(HttpStatus.BAD_REQUEST)
                  .body("Invalid file type. Please upload a CSV file.");
      }

      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      String response = s3FileService
              .uploadCSV(tenantId, file, insightsData);
      return ResponseEntity
              .status(HttpStatus.OK).body(response);
    } catch (JsonProcessingException e) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST).build();
    }

  }

  @PostMapping("{tenantId}/upload-faq") // Not used
  @PreAuthorize("hasAuthority('tenant_create')")
  public ResponseEntity<String> uploadFAQ(
    @PathVariable(name = "tenantId") Long tenantId,
    @RequestPart MultipartFile file
  ) {
    // check if file type is csv
    if (!file.getContentType().equals("text/csv")) {
      return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body("Invalid file type. Please upload a CSV file.");
    }

    String response = s3FileService.uploadFAQ(tenantId, file);
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping("/documents")
  public ResponseEntity<TenantLegalDocRespDTO> getTenantDocuments(
    @RequestParam(name = "tenantId") Long tenantId) {
    TenantLegalDocRespDTO responseDTO = tenantService
      .getTenantLinks(tenantId);
    return ResponseEntity.ok(responseDTO);
  }

  @GetMapping("/{tenantId}/metadata")
  @PreAuthorize("hasAuthority('tenant_read')")
  public ResponseEntity<FeatureResponseDto> getTenantFeatures(
          @PathVariable Long tenantId,
          @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      Long authenticatedTenantId = (Long) authenticationDetailUtil
              .getAuthenticationDetails("tenantid");
      if (!tenantId.equals(authenticatedTenantId)) {
          return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      FeatureResponseDto features = tenantService
              .getFeaturesByTenantId(tenantId, insightsData);
      return new ResponseEntity<>(features, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST).build();
    }

  }

  @GetMapping("/metadata") // Platform admin
  @PlatformAdminGuard
  public ResponseEntity<FeatureResponseDto> getTenantFeaturesForPlatform(
          @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      FeatureResponseDto features = tenantService
              .getFeaturesForPlatform(insightsData);
      return new ResponseEntity<>(features, HttpStatus.OK);
    } catch (JsonProcessingException e) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST).build();
    }

  }


  @GetMapping("/{tenantId}/features")
  @PreAuthorize("hasAuthority('tenant_read')")
  public ResponseEntity<List<FeatureDTO>> getAllFeatures(
          @PathVariable Long tenantId,
          @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      List<FeatureDTO> features = tenantService
              .getAllFeaturesWithStatus(tenantId, insightsData);
      return ResponseEntity.ok(features);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

  }

  @PostMapping("/feature") //Not used
  @PreAuthorize("hasAuthority('tenant_create')")
  public ResponseEntity<String> addFeatureToTenant(
    @RequestParam Long tenantId,
    @RequestParam Long featureId,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      tenantService.addFeatureToTenant(tenantId, featureId, insightsData);
      return ResponseEntity.ok("Feature added to tenant successfully.");
    } catch (JsonProcessingException e) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST).build();
    }

  }

  @DeleteMapping("/{tenantId}/feature/{featureId}") //Not used
  @PreAuthorize("hasAuthority('tenant_delete')")
  public ResponseEntity<String> removeFeatureFromTenant(
    @PathVariable Long tenantId,
    @PathVariable Long featureId,
    @RequestHeader("X-Insights-Data") String insightsDataJson
  ) {
    try {
      InsightsFrontendDto insightsData = objectMapper
              .readValue(insightsDataJson, InsightsFrontendDto.class);
      tenantService
              .removeFeatureFromTenant(tenantId, featureId, insightsData);
      return ResponseEntity.ok("Feature removed from tenant successfully");
    } catch (JsonProcessingException e) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST).build();
    }

  }

}
