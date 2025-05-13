package com.rtb.tenant.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.rtb.core.entity.tenant.Feature;
import com.rtb.core.entity.tenant.FeaturePermission;
import com.rtb.core.entity.user.Permission;
import com.rtb.core.entity.tenant.Tenant;
import com.rtb.core.entity.tenant.TenantCommunication;
import com.rtb.core.entity.tenant.TenantCommunicationChannels;
import com.rtb.core.entity.tenant.TenantFeature;
import com.rtb.core.enums.CommunicationChannel;
import com.rtb.tenant.dto.FeatureResponseDto;
import com.rtb.tenant.dto.Tenant_Insights.Request.InsightsFrontendDto;
import com.rtb.tenant.dto.Tenant_Insights.Request.TenantInsightDto;
import com.rtb.tenant.dto.UpdateChannels;

import com.rtb.core.enums.CommunicationCategory;
import com.rtb.tenant.exception.BadRequestException;
import com.rtb.tenant.exception.InternalServerErrorException;
import com.rtb.tenant.exception.ResourceNotFoundException;
import com.rtb.tenant.exception.IllegalArgumentException;
import com.rtb.core.repository.TenantFeatureRepository;
import com.rtb.core.repository.UserRepository;
import com.rtb.core.repository.TenantCommunicationRepository;
import com.rtb.core.repository.ChannelsRepository;
import com.rtb.core.repository.FeatureRepository;
import com.rtb.core.repository.FeaturePermissionRepository;
import com.rtb.core.repository.TenantRepository;
import com.rtb.core.repository.PermissionRepository;

import com.rtb.tenant.dto.FeatureDTO;
import com.rtb.tenant.dto.KafkaProduceEventDto;
import com.rtb.tenant.dto.TenantCommunicationDto;
import com.rtb.tenant.dto.TenantCommunications;
import com.rtb.tenant.dto.TenantLegalDocRespDTO;
import com.rtb.tenant.dto.TenantUserCountDTO;
import com.rtb.tenant.dto.UpdateCommunicationChannelDetails;
import com.rtb.tenant.dto.UpdateList;

import com.rtb.tenant.utls.AuthenticationDetailUtil;
import com.rtb.tenant.utls.InsightsEventId;
import com.rtb.tenant.utls.InsightsMessages;
import com.rtb.tenant.utls.Constants;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TenantService {

  private final ObjectMapper objectMapper;

  private final TenantRepository tenantRepository;

  private final UserRepository userRepository;

  private final WebClient webClient;

  private final TenantFeatureRepository tenantFeatureRepository;

  private final TenantCommunicationRepository tenantCommunicationRepository;

  private final ChannelsRepository channelsRepository;

  private final FeatureRepository featureRepository;

  private InsightEventService insightEventService;

  private final AuthenticationDetailUtil authenticationDetailUtil;
  private final FeaturePermissionRepository featurePermissionRepository;
  private final PermissionRepository permissionRepository;

  public TenantService(
          ObjectMapper objectMapper,
          TenantRepository tenantRepository,
          UserRepository userRepository,
          WebClient.Builder webClientBuilder,
          FeatureRepository featureRepository,
          TenantFeatureRepository tenantFeatureRepository,
          @Value("${MESSAGE_BUS_URL:http://3.105.243.131:8080}")
      String messageBusUrl,
          TenantCommunicationRepository tenantCommunicationRepository,
          ChannelsRepository channelsRepository,
          InsightEventService insightEventService,
          AuthenticationDetailUtil authenticationDetailUtil,
          FeaturePermissionRepository featurePermissionRepository,
          PermissionRepository permissionRepository
  ) {
    this.objectMapper = objectMapper;
    this.tenantRepository = tenantRepository;
    this.userRepository = userRepository;
    this.tenantFeatureRepository = tenantFeatureRepository;
    this.featureRepository = featureRepository;
    this.tenantCommunicationRepository = tenantCommunicationRepository;
    this.channelsRepository = channelsRepository;
      this.featurePermissionRepository = featurePermissionRepository;
      this.permissionRepository = permissionRepository;
      this.webClient =
            webClientBuilder.baseUrl(messageBusUrl).build();
    this.insightEventService = insightEventService;
    this.authenticationDetailUtil = authenticationDetailUtil;

  }


  public Tenant createTenant(Tenant tenant, InsightsFrontendDto insightsData) {
    Long authenticatedUserId =
            (Long) authenticationDetailUtil
                    .getAuthenticationDetails("id");

    TenantInsightDto insightDto = TenantInsightDto.builder()
                    .insightsDataFrontend(insightsData)
                            .build();
    insightEventService.sendEvents(InsightsEventId
                    .CREATE_TENANT_REQUEST_RECEIVED, insightDto,
            InsightsMessages.CREATE_TENANT_RECEIVED,
            true, 202, 0L, authenticatedUserId);

    try {
      tenant = tenantRepository.save(tenant);
    } catch (Exception e) {
      insightEventService.sendEvents(InsightsEventId
                      .CREATE_TENANT_FAILURE, insightDto,
              InsightsMessages.UNEXPECTED_ERROR, true, 500, 0L, authenticatedUserId);
      throw new RuntimeException("An unexpected error occurred");
    }
    JsonNode tenantjson = convertUserToPayload(tenant);
    KafkaProduceEventDto kafkaProduceEventDto =
            new KafkaProduceEventDto();
    kafkaProduceEventDto.setOrigin("tenant");
    kafkaProduceEventDto.setEventname("topic");
    kafkaProduceEventDto.setPayload(tenantjson);
    // Send event to Message Bus service via HTTP POST
    webClient.post()
      .uri("/api/v1/messageBus/event/"
              + tenant.getId())
      .bodyValue(kafkaProduceEventDto)
      .retrieve()
      .bodyToMono(Void.class)
      .subscribe();  // Subscribe is non-blocking

      for (CommunicationChannel channel : CommunicationChannel.values()) {
        TenantCommunicationChannels tenantChannel =
                new TenantCommunicationChannels();
        tenantChannel.setTenant(tenant);
        tenantChannel.setCommunicationChannel(channel);
        tenantChannel.setActive(false);

        channelsRepository.save(tenantChannel);
      }

    insightEventService.sendEvents(InsightsEventId
                    .CREATE_TENANT_SUCCESS, insightDto,
            InsightsMessages.CREATE_TENANT_SUCCESS, true,
            201, tenant.getId(), authenticatedUserId);

    return tenant;

  }

  private ObjectNode convertUserToPayload(Tenant tenant) {
    try {
      // Convert user object directly to JsonNode
      return objectMapper.valueToTree(tenant);
    } catch (Exception e) {
      System.err.println("Error converting user object to JsonNode: "
              + e.getMessage());
      return objectMapper.createObjectNode();
    }
  }

  public Tenant enableTenant(Long tenantId, InsightsFrontendDto insightsData) {
    Long authenticatedUserId =
            (Long) authenticationDetailUtil
                    .getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();
    try {

      insightEventService.sendEvents(
              InsightsEventId
                      .ENABLE_TENANT_REQUEST_RECEIVED, tenantInsightDto,
              InsightsMessages.ENABLE_TENANT_RECEIVED, true,
              202, 1L, authenticatedUserId);

      Tenant tenant = tenantRepository.findById(tenantId)
              .orElseThrow(() -> {
                insightEventService.sendEvents(InsightsEventId
                                .ENABLE_TENANT_FAILURE, tenantInsightDto,
                        InsightsMessages.TENANT_NOT_FOUND,
                        false, 404, tenantId, authenticatedUserId);
                throw new ResourceNotFoundException(
                        String.format("Tenant not found with id: %s ", tenantId));
              });

      tenant.setEnabled(true);
      Tenant savedTenant = tenantRepository.save(tenant);

      if (savedTenant == null) {
        insightEventService.sendEvents(
                InsightsEventId.ENABLE_TENANT_FAILURE, tenantInsightDto,
                InsightsMessages.SAVE_FAILED, false, 500,
                1L, authenticatedUserId);
        throw new IllegalArgumentException("Failed to save enabled Tenant");
      }

      insightEventService.sendEvents(InsightsEventId
                      .ENABLE_TENANT_SUCCESS, tenantInsightDto,
              InsightsMessages.ENABLE_TENANT_SUCCESS, true,
              200, 1L, authenticatedUserId);

      return savedTenant;

    } catch (ResourceNotFoundException | IllegalArgumentException ex) {
      insightEventService.sendEvents(InsightsEventId
                      .ENABLE_TENANT_FAILURE, tenantInsightDto,
              InsightsMessages.NOT_FOUND, false, 500, 1L, authenticatedUserId);
      throw ex;
    } catch (Exception ex) {
      insightEventService.sendEvents(InsightsEventId
                      .ENABLE_TENANT_FAILURE, tenantInsightDto,
              InsightsMessages.UNEXPECTED_ERROR, false, 500,
              0L, authenticatedUserId);
      throw new InternalServerErrorException(
              "An unexpected error occurred while enabling the tenant");
    }
  }



    public Tenant disableTenant(Long tenantId, InsightsFrontendDto insightsDataFrontend) {
        Long authenticatedUserId = (Long) authenticationDetailUtil.getAuthenticationDetails("id");

        try {
            insightEventService.sendEvents(
                    InsightsEventId.DISABLE_TENANT_REQUEST_RECEIVED,
                    insightsDataFrontend,
                    InsightsMessages.DISABLE_TENANT_RECEIVED,
                    true,
                    202,
                    0L,
                    authenticatedUserId
            );

            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> {
                        insightEventService.sendEvents(
                                InsightsEventId.DISABLE_TENANT_FAILURE,
                                insightsDataFrontend,
                                InsightsMessages.TENANT_NOT_FOUND,
                                false,
                                404,
                                0L,
                                authenticatedUserId
                        );
                        return new ResourceNotFoundException(
                                String.format("Tenant not found with id: %s", tenantId)
                        );
                    });


            tenant.setEnabled(false);
            Tenant savedTenant = tenantRepository.save(tenant);

            if (savedTenant == null) {
                insightEventService.sendEvents(
                        InsightsEventId.DISABLE_TENANT_FAILURE,
                        insightsDataFrontend,
                        InsightsMessages.SAVE_FAILED,
                        false,
                        500,
                        0L,
                        authenticatedUserId
                );
                throw new IllegalStateException("Failed to save the disabled tenant.");
            }

            insightEventService.sendEvents(
                    InsightsEventId.DISABLE_TENANT_SUCCESS,
                    insightsDataFrontend,
                    InsightsMessages.DISABLE_TENANT_SUCCESS,
                    true,
                    200,
                    0L,
                    authenticatedUserId
            );

            return savedTenant;

        } catch (ResourceNotFoundException | IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            insightEventService.sendEvents(
                    InsightsEventId.DISABLE_TENANT_FAILURE,
                    insightsDataFrontend,
                    InsightsMessages.UNEXPECTED_ERROR,
                    false,
                    500,
                    0L,
                    authenticatedUserId
            );
            throw new InternalServerErrorException(
                    "An unexpected error occurred while disabling the tenant"
            );
        }
    }



    public List<TenantUserCountDTO> getTenants(
          InsightsFrontendDto insightsData
  ) {
    Long authenticatedUserId = (Long) authenticationDetailUtil
            .getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();
    try {
      insightEventService.sendEvents(InsightsEventId
                      .GET_ALL_TENANTS_REQUEST_RECEIVED, tenantInsightDto,
              InsightsMessages.GET_ALL_TENANTS_WITH_USER_COUNT_RECEIVED, true,
              202, 0L, authenticatedUserId);
      List<TenantUserCountDTO> tenantList
              = tenantRepository.findAll().stream()
              .map(tenant -> TenantUserCountDTO.builder()
                      .tenantId(tenant.getId())
                      .longName(tenant.getLongName())
                      .logo(tenant.getLogo())
                      .email(tenant.getEmail())
                      .shortName(tenant.getShortName())
                      .userCount(userRepository.countByTenantId(tenant.getId()))
                      .isEnabled(tenant.isEnabled())
                      .build()
              )
              .collect(Collectors.toList());

      insightEventService.sendEvents(InsightsEventId
                      .GET_ALL_TENANTS_SUCCESS, tenantInsightDto,
              InsightsMessages.GET_ALL_TENANTS_WITH_USER_COUNT_SUCCESS, true,
              200, 0L, authenticatedUserId);

      return tenantList;

    } catch (Exception ex) {
      insightEventService.sendEvents(InsightsEventId
                      .GET_ALL_TENANTS_FAILURE, tenantInsightDto,
              InsightsMessages.UNEXPECTED_ERROR, true,
              500, 0L, authenticatedUserId);
      throw new InternalServerErrorException(
              "An unexpected error occurred while retrieving tenant data");
    }
  }


  public Tenant getTenantId(
          Long tenantId, InsightsFrontendDto insightsData
  ) {
    Long authenticatedUserId =
            (Long) authenticationDetailUtil.getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
                    .insightsDataFrontend(insightsData)
                            .build();
    insightEventService.sendEvents(InsightsEventId
                    .GET_TENANT_BY_ID_REQUEST_RECEIVED, tenantInsightDto,
            InsightsMessages.GET_TENANT_DETAILS_BY_ID_RECEIVED, true,
            202, 0L, authenticatedUserId);

    Tenant tenant = tenantRepository.findById(tenantId)
      .orElseThrow(() -> {
                insightEventService.sendEvents(InsightsEventId
                                .GET_TENANT_BY_ID_FAILURE, tenantInsightDto,
                        InsightsMessages.TENANT_NOT_FOUND, true,
                        404, 0L, authenticatedUserId);

                throw new ResourceNotFoundException(
                        "Tenant Not Found with id " + tenantId);
              }
        );

    insightEventService.sendEvents(InsightsEventId
                    .GET_TENANT_BY_ID_SUCCESS, tenantInsightDto,
            InsightsMessages.GET_TENANT_DETAILS_BY_ID_SUCCESS, true,
             200, 0L, authenticatedUserId);

    return tenant;
  }

  public boolean checkTenants(
          Long tenantId
  ) {
    return tenantRepository.existsById(tenantId);
  }

  public Tenant updateTenant(Long tenantId, Tenant tenantDetails,
                             InsightsFrontendDto insightsData) {
    Long authenticatedUserId = (Long) authenticationDetailUtil
            .getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();
    insightEventService.sendEvents(InsightsEventId
                    .UPDATE_TENANT_REQUEST_RECEIVED, tenantInsightDto,
            InsightsMessages.UPDATE_TENANT_DETAILS_RECEIVED, true,
            202, 0L, authenticatedUserId);

    Tenant tenant = tenantRepository.findById(tenantId)
      .orElseThrow(() -> {
                insightEventService.sendEvents(InsightsEventId
                                .UPDATE_TENANT_FAILURE, tenantInsightDto,
                        InsightsMessages.TENANT_NOT_FOUND, true,
                        500, 0L, authenticatedUserId);

                throw new ResourceNotFoundException("Tenant not found with id: "
                        + tenantId);      }
        );
    if (Objects.nonNull(tenantDetails.getLongName())
      && !tenantDetails.getLongName().isEmpty()) {
      tenant.setLongName(tenantDetails.getLongName());
    }

    // Update the email if provided
    if (Objects.nonNull(tenantDetails.getEmail())
      && !tenantDetails.getEmail().isEmpty()) {
      tenant.setEmail(tenantDetails.getEmail());
    }

    if (Objects.nonNull(tenantDetails.getDescription())
      && !tenantDetails.getDescription().isEmpty()) {
      tenant.setDescription(tenantDetails.getDescription());
    }

    if (Objects.nonNull(tenantDetails.getLogo())
      && !tenantDetails.getLogo().isEmpty()) {
      tenant.setLogo(tenantDetails.getLogo());
    }

    if (Objects
            .nonNull(tenantDetails.getShortName())
      && !tenantDetails.getShortName().isEmpty()) {
      tenant.setShortName(tenantDetails.getShortName());
    }

    if (Objects
            .nonNull(tenantDetails.getAddress())
      && !tenantDetails.getAddress().isEmpty()) {
      tenant.setAddress(tenantDetails.getAddress());
    }

    if (Objects
            .nonNull(tenantDetails.getPhoneNumber())
      && !tenantDetails.getPhoneNumber().isEmpty()) {
      tenant.setPhoneNumber(tenantDetails.getPhoneNumber());
    }

    if (Objects
            .nonNull(tenantDetails.getPrimaryColor())
      && !tenantDetails.getPrimaryColor().isEmpty()) {
      tenant.setPrimaryColor(tenantDetails
              .getPrimaryColor());
    }

    if (Objects.nonNull(tenantDetails
            .getSecondaryColor())
      && !tenantDetails.getSecondaryColor().isEmpty()) {
      tenant.setSecondaryColor(tenantDetails
              .getSecondaryColor());
    }

    if (Objects.nonNull(tenantDetails
            .getPrivacyPolicyLink())
      && !tenantDetails.getPrivacyPolicyLink().isEmpty()) {
      tenant.setPrivacyPolicyLink(tenantDetails
              .getPrivacyPolicyLink());
    }

    if (Objects.nonNull(tenantDetails
            .getTermsConditionsLink())
      && !tenantDetails.getTermsConditionsLink().isEmpty()) {
      tenant.setTermsConditionsLink(tenantDetails
              .getTermsConditionsLink());
    }

    if (Objects.nonNull(tenantDetails
            .getAboutUsLink())
      && !tenantDetails.getAboutUsLink().isEmpty()) {
      tenant.setAboutUsLink(tenantDetails
              .getAboutUsLink());
    }

    if (Objects.nonNull(tenantDetails
            .getUploadCsvFaq())
      && !tenantDetails.getUploadCsvFaq().isEmpty()) {
      tenant.setUploadCsvFaq(tenantDetails
              .getUploadCsvFaq());
    }

    // Update the YouTube handle if provided
    if (Objects.nonNull(tenantDetails.getYoutubeHandle())
      && !tenantDetails.getYoutubeHandle().isEmpty()) {
      tenant.setYoutubeHandle(tenantDetails.getYoutubeHandle());
    }

    // Update the Twitter handle if provided
    if (Objects.nonNull(tenantDetails
            .getTwitterHandle())
      && !tenantDetails.getTwitterHandle().isEmpty()) {
      tenant.setTwitterHandle(tenantDetails
              .getTwitterHandle());
    }

    // Update the API key if provided
    if (Objects.nonNull(tenantDetails.getApiKey())
            && !tenantDetails.getApiKey().isEmpty()) {
      tenant.setApiKey(tenantDetails.getApiKey());
    }

    // Save the updated tenant back to the database
    Tenant savedTenant =  tenantRepository.save(tenant);

    insightEventService.sendEvents(InsightsEventId
                    .UPDATE_TENANT_SUCCESS, tenantInsightDto,
            InsightsMessages.UPDATE_TENANT_DETAILS_SUCCESS, true,
            200, 0L, authenticatedUserId);

    return savedTenant;
  }

  public List<String> getCommunicationCategories() {
    List<CommunicationCategory> categories =
            List.of(CommunicationCategory.values());
    return categories.stream()
      .map(CommunicationCategory::toString)
      .collect(Collectors.toList());
  }

  public Tenant addCommunication(
          Long tenantId, TenantCommunications tenantCommunications,
          InsightsFrontendDto insightsData
  ) {
    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();

    Long authenticatedUserId = (Long) authenticationDetailUtil
            .getAuthenticationDetails("id");

    insightEventService
            .sendEvents(InsightsEventId
                            .CREATE_COMMUNICATION_TEMPLATE_REQUEST_RECEIVED,
                    tenantInsightDto,
            InsightsMessages.CREATE_COMMUNICATION_TEMPLATE_RECEIVED, true,
                    202, 0L, authenticatedUserId);

    Tenant tenant = tenantRepository.findById(tenantId)
      .orElseThrow(
        () -> {
          insightEventService.sendEvents(InsightsEventId
                          .CREATE_COMMUNICATION_TEMPLATE_FAILURE, tenantInsightDto,
                  InsightsMessages.TENANT_NOT_FOUND, false, 404, 0L, authenticatedUserId);

          throw new ResourceNotFoundException(
                  String.format("Tenant not found with id: %s", tenantId));
        }
      );

    for (
            TenantCommunicationDto
                    tenantCommunicationDto : tenantCommunications.communications()
    ) {
      // check if the template is already present for the tenant with the same category
      if (tenantCommunicationRepository.existsByTenantAndCategoryAndChannel(
              tenant,
              CommunicationCategory.fromString(tenantCommunicationDto.category()),
              getComChannel.apply(tenantCommunicationDto.communicationChannel())).isPresent()) {
        insightEventService.sendEvents(InsightsEventId
                        .CREATE_COMMUNICATION_TEMPLATE_FAILURE, tenantInsightDto,
                InsightsMessages.TEMPLATE_ALREADY_EXISTS, false, 404, 0L, authenticatedUserId);

        throw new ResourceNotFoundException(
                "Template already exists for the tenant with category "
                  + tenantCommunicationDto.category());
      }

      TenantCommunication channel = TenantCommunication.builder()
        .tenant(tenant)
        .communicationChannel(getComChannel.apply(
          tenantCommunicationDto.communicationChannel())
        )
        .category(CommunicationCategory
                .fromString(tenantCommunicationDto.category()))
        .templateUrl(tenantCommunicationDto.templateUrl())
        .active(Boolean.TRUE)
        .build();

      tenantCommunicationRepository.save(channel);

      insightEventService.sendEvents(InsightsEventId
                      .CREATE_COMMUNICATION_TEMPLATE_SUCCESS, tenantInsightDto,
              InsightsMessages.CREATE_COMMUNICATION_TEMPLATE_SUCCESS, true,
              201, 0L, authenticatedUserId);

    }

    return getTenantDetailsById(tenantId);

  }

  public void updateCommunicationDetails(
    UpdateList requestList,
    Long tenantId,
    Long communicationId,
    InsightsFrontendDto insightsData
  ) {
    Long authenticatedTenantId = (Long) authenticationDetailUtil
            .getAuthenticationDetails("tenantid");
    Long authenticatedUserId = (Long) authenticationDetailUtil.getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
                    .insightsDataFrontend(insightsData).
            communicationId(communicationId)
                    .build();
    insightEventService.sendEvents(InsightsEventId
                    .UPDATE_COMMUNICATION_TEMPLATE_REQUEST_RECEIVED, tenantInsightDto,
            InsightsMessages.UPDATE_COMMUNICATION_CHANNEL_TEMPLATE_RECEIVED,
            true, 202, 0L, authenticatedUserId);


    for (
            UpdateCommunicationChannelDetails request : requestList.communications()
    ) {

      Tenant tenant =
              getTenantId(request.tenantId(), insightsData);

      TenantCommunication tenantCommunication = tenantCommunicationRepository
        .findByTenantAndId(tenant, request.communicationId())
        .orElseThrow(
          () -> {
            insightEventService.sendEvents(InsightsEventId
                            .UPDATE_COMMUNICATION_TEMPLATE_FAILURE, tenantInsightDto,
                    InsightsMessages.COMMUNICATION_CHANNEL_NOT_FOUND, false,
                    404, 0L, authenticatedUserId);
            throw new ResourceNotFoundException(
                    "Communication not found");
          }
        );

      if (!Objects.isNull(request.active())) {
        tenantCommunication
                .setActive(activeLookUp(request.active()));
      }

      if (!Objects.isNull(request.category())) {
          tenantCommunication
                  .setCategory(CommunicationCategory
                          .fromString(request.category()));
      }

      if (!Objects.isNull(request.templateUrl())) {
        tenantCommunication.setTemplateUrl(
                request.templateUrl());
      }

      tenantRepository.save(tenant);

      insightEventService.sendEvents(InsightsEventId
                      .UPDATE_COMMUNICATION_TEMPLATE_SUCCESS, tenantInsightDto,
              InsightsMessages.UPDATE_COMMUNICATION_CHANNEL_TEMPLATE_SUCCESS,
              true, 201, 0L, authenticatedUserId);


    }

  }

  public void deleteCommunications(
          Long tenantId, Long communicationsId,
          InsightsFrontendDto insightsFrontendDto) {
    Long authenticatedUserId
            = (Long) authenticationDetailUtil
            .getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
            .insightsDataFrontend(insightsFrontendDto)
            .communicationId(communicationsId)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .DELETE_COMMUNICATION_CHANNEL_TEMPLATE_REQUEST_RECEIVED,
            tenantInsightDto,
            InsightsMessages.DELETE_COMMUNICATION_CHANNEL_TEMPLATE_RECEIVED,
            true, 202, 0L, authenticatedUserId);

    Tenant tenant = getTenantId(tenantId, insightsFrontendDto);

    TenantCommunication tenantCommunication
            = tenantCommunicationRepository
      .findByTenantAndId(tenant, communicationsId)
      .orElseThrow(
        () -> {
          insightEventService.sendEvents(InsightsEventId
                          .DELETE_COMMUNICATION_CHANNEL_TEMPLATE_FAILURE, tenantInsightDto,
                  InsightsMessages.COMMUNICATION_CHANNEL_NOT_FOUND, false,
                  404, 0L, authenticatedUserId);

          throw new ResourceNotFoundException(
                  "Communication not found");
        }
      );

    tenantCommunicationRepository
            .delete(tenantCommunication);

    insightEventService.sendEvents(InsightsEventId
                    .DELETE_COMMUNICATION_CHANNEL_TEMPLATE_SUCCESS, tenantInsightDto,
            InsightsMessages.DELETE_COMMUNICATION_CHANNEL_TEMPLATE_SUCCESS,
            true, 200, 0L, authenticatedUserId);

  }

  public Set<TenantCommunicationChannels> getAllChannels(
          Long tenantId) {
      Tenant tenant = getTenantId(tenantId, null);
      Set<TenantCommunicationChannels> data = channelsRepository.findAllByTenant(tenant);
      Set<CommunicationChannel> categories = new HashSet<>(getCommunicationChannels());

      if (data.size() == categories.size()) {
        return data;
      }

      for (TenantCommunicationChannels channels: data) {
          categories.remove(channels.getCommunicationChannel());
      }

      for (CommunicationChannel category: categories) {
          TenantCommunicationChannels channel = new TenantCommunicationChannels();
          channel.setTenant(tenant);
          channel.setCommunicationChannel(category);
          channel.setActive(false);

          channelsRepository.save(channel);
      }

      return channelsRepository.findAllByTenant(tenant);
  }

  private List<CommunicationChannel> getCommunicationChannels() {
    return List.of(CommunicationChannel.values());
  }

  public void updateChannels(List<UpdateChannels> updateChannels,
                             InsightsFrontendDto insightsData) {
    Long authenticatedUserId = (Long) authenticationDetailUtil.getAuthenticationDetails("id");

    TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .UPDATE_COMMUNICATION_CHANNEL_STATUS_REQUEST_RECEIVED, tenantInsightDto,
            InsightsMessages.UPDATE_COMMUNICATION_CHANNEL_STATE_RECEIVED, true,
            202, 0L, authenticatedUserId);

    for (
            UpdateChannels channel : updateChannels
    ) {
        TenantCommunicationChannels tenantCommunicationChannels = channelsRepository
            .findById(channel.id()).orElseThrow(
                () -> {
                  insightEventService.sendEvents(InsightsEventId
                                  .UPDATE_COMMUNICATION_CHANNEL_STATUS_FAILURE, tenantInsightDto,
                          InsightsMessages.COMMUNICATION_CHANNEL_NOT_FOUND, false,
                          500, 0L, authenticatedUserId);

                  throw new ResourceNotFoundException(
                          "Such channel doesn't exist");
                }
            );

        if (channel.active().equals(
                tenantCommunicationChannels.getActive())) {
          insightEventService.sendEvents(InsightsEventId
                          .UPDATE_COMMUNICATION_CHANNEL_STATUS_FAILURE, tenantInsightDto,
                  InsightsMessages.COMMUNICATION_CHANNEL_NOT_FOUND,
                  false, 500, 0L, authenticatedUserId);

          throw new IllegalArgumentException(
                  "The status already the same");
        }

        tenantCommunicationChannels
                .setActive(channel.active());

        channelsRepository
                .save(tenantCommunicationChannels);

      insightEventService.sendEvents(InsightsEventId
                      .UPDATE_COMMUNICATION_CHANNEL_STATUS_SUCCESS, tenantInsightDto,
              InsightsMessages.UPDATE_COMMUNICATION_CHANNEL_STATE_SUCCESS,
              true, 200, 0L, authenticatedUserId);

    }
  }

  private Boolean activeLookUp(String active) {
    return switch (active.toLowerCase()) {
      case "active" -> Boolean.TRUE;
      case "inactive" -> Boolean.FALSE;
      default -> throw new ResourceNotFoundException("Illegal status");
    };
  }

  private final Function<String, Boolean> statusLoopUp = (status) -> {
    if (status.equalsIgnoreCase(Constants.ACTIVE)) {
      return Boolean.TRUE;
    } else if (status.equalsIgnoreCase(Constants.INACTIVE)) {
      return Boolean.FALSE;
    } else {
      throw new IllegalArgumentException(Constants.ILLEGAL_COMMUNICATION_STATUS);
    }
  };

  private final Function<String, CommunicationChannel>
    getComChannel = (channel) ->
    switch (channel.toLowerCase()) {
      case Constants.EMAIL -> CommunicationChannel.EMAIL;
      case Constants.SMS -> CommunicationChannel.SMS;
      case Constants.PUSH -> CommunicationChannel.PUSH;
      default ->
              throw new ResourceNotFoundException(
                      Constants.INVALID_CHANNEL);
    };

  private Tenant getTenantDetailsById(Long tenantId) {
    return tenantRepository.findById(tenantId)
      .orElseThrow(
        () -> new ResourceNotFoundException(Constants.TENANT_NOT_FOUND)
      );
  }

  public TenantLegalDocRespDTO getTenantLinks(Long tenantId) {
    Long authenticatedTenantId = (Long) authenticationDetailUtil
            .getAuthenticationDetails("tenantid");
    if (!tenantId.equals(authenticatedTenantId)) {
      throw new BadRequestException("Invalid Tenant");
    }
    Tenant tenant = tenantRepository.findById(tenantId)
      .orElseThrow(() -> new ResourceNotFoundException("Tenant not found with id:" + tenantId));

    return new TenantLegalDocRespDTO(
      tenant.getPrivacyPolicyLink(),
      tenant.getTermsConditionsLink(),
      tenant.getAboutUsLink()
    );

  }

  public FeatureResponseDto getFeaturesByTenantId(
          Long tenantId, InsightsFrontendDto insightsData
  ) {

    Long authenticatedUserId =
            (Long) authenticationDetailUtil
                    .getAuthenticationDetails("id");

    TenantInsightDto tenantInsight = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .GET_METADATA_OF_FEATURES_REQUEST_RECEIVED, tenantInsight,
            InsightsMessages.GET_METADATA_OF_FEATURES_RECEIVED, true,
            202, 0L, authenticatedUserId);

    boolean tenantEnabled = false;
    boolean allUsersEnabled = false;
    boolean tenantUserTicketsEnabled = false;
    boolean tenantAdminTicketsEnabled = false;

    // Fetch features based on tenantId
    List<Feature> featureList = featureRepository.findFeaturesByTenantId(tenantId);
    if (featureList.isEmpty()) {
      insightEventService.sendEvents(InsightsEventId
                      .GET_METADATA_OF_FEATURES_FAILURE, tenantInsight,
              InsightsMessages.FEATURE_NOT_FOUND, false, 404,
              0L, authenticatedUserId);

      return null; // Return an empty list if no features are found
    }

    for (Feature feature : featureList) {
      if ("Tenants".equals(feature.getFeatureName())) {
        tenantEnabled = true;
      }
      if ("All Users".equals(feature.getFeatureName())) {
        allUsersEnabled = true;
      }
      if ("Tenant User Tickets".equals(
              feature.getFeatureName())) {
        tenantUserTicketsEnabled = true;
      }
      if ("Tenant Admin Tickets".equals(
              feature.getFeatureName())) {
          tenantAdminTicketsEnabled = true;
      }
    }
    boolean administrationEnabled =
            tenantEnabled || allUsersEnabled;
    boolean supportEnabled =
            tenantUserTicketsEnabled;

    if (administrationEnabled) {
      Feature result = featureRepository
              .findByFeatureName("Administration");
      featureList.add(result);
    }
    if (supportEnabled) {
      Feature result = featureRepository
              .findByFeatureName("Support");
      featureList.add(result);
    }

    insightEventService.sendEvents(InsightsEventId
                    .GET_METADATA_OF_FEATURES_SUCCESS, tenantInsight,
            InsightsMessages.GET_METADATA_OF_FEATURES_SUCCESS, true,
            200, 0L, authenticatedUserId);

    // Collect feature IDs into a Set<Long>
    Set<FeatureDTO> featureDTOs = featureList.stream()
            .map(feature -> new FeatureDTO(feature.getId(),
                    feature.getFeatureName(),
                    feature.getFeatureDescription(),
                    true))
            .collect(Collectors.toSet());

    // Collect feature IDs
    Set<Long> featureIds = featureList.stream()
            .map(Feature::getId)
            .collect(Collectors.toSet());

    // Fetch all permissions for the collected feature IDs
    List<FeaturePermission> featurePermissions =
            featurePermissionRepository.findByFeatureIdIn(featureIds);

    // Collect all permissions
    List<String> permissions = featurePermissions.stream()
            .map(fp -> fp.getPermission().getPermissionName())  // Extract permission string
            .collect(Collectors.toList());

    return new FeatureResponseDto(featureDTOs, permissions);
  }

  public FeatureResponseDto getFeaturesForPlatform(InsightsFrontendDto insightsData
  ) {

    Long authenticatedUserId =
            (Long) authenticationDetailUtil
                    .getAuthenticationDetails("id");

    TenantInsightDto tenantInsight = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .GET_METADATA_OF_FEATURES_REQUEST_RECEIVED, tenantInsight,
            InsightsMessages.GET_METADATA_OF_FEATURES_RECEIVED, true,
            202, 0L, authenticatedUserId);

    boolean tenantEnabled = false;
    boolean allUsersEnabled = false;
    boolean tenantUserTicketsEnabled = false;
    boolean tenantAdminTicketsEnabled = false;

    List<String> permissions = (List<String>) authenticationDetailUtil
                    .getAuthenticationDetails("permissions");

    List<Long> permissionIds = permissions.stream()
            .map(permission -> {
              Permission permissionObj = permissionRepository
                      .findByPermissionName(permission);
              return permissionObj.getId(); // Convert Long to Integer
            })
            .toList();



    // Fetch features based on tenantId
    List<FeaturePermission> featurePermissionList =
            featurePermissionRepository.findByPermissionIdIn(permissionIds);
    Set<Feature> featureList = featurePermissionList.stream()
            .map(FeaturePermission::getFeature)
            .collect(Collectors.toSet());
    if (featureList.isEmpty()) {
      insightEventService.sendEvents(InsightsEventId
                      .GET_METADATA_OF_FEATURES_FAILURE, tenantInsight,
              InsightsMessages.FEATURE_NOT_FOUND, false, 404,
              0L, authenticatedUserId);

      return null; // Return an empty list if no features are found
    }

    for (Feature feature : featureList) {
      if ("Tenants".equals(feature.getFeatureName())) {
        tenantEnabled = true;
      }
      if ("All Users".equals(feature.getFeatureName())) {
        allUsersEnabled = true;
      }
      if ("Tenant User Tickets".equals(
              feature.getFeatureName())) {
        tenantUserTicketsEnabled = true;
      }
      if ("Tenant Admin Tickets".equals(
              feature.getFeatureName())) {
        tenantAdminTicketsEnabled = true;
      }
    }
    boolean administrationEnabled =
            tenantEnabled || allUsersEnabled;
    boolean supportEnabled =
            tenantUserTicketsEnabled;

    if (administrationEnabled) {
      Feature result = featureRepository
              .findByFeatureName("Administration");
      featureList.add(result);
    }
    if (supportEnabled) {
      Feature result = featureRepository
              .findByFeatureName("Support");
      featureList.add(result);
    }

    insightEventService.sendEvents(InsightsEventId
                    .GET_METADATA_OF_FEATURES_SUCCESS, tenantInsight,
            InsightsMessages.GET_METADATA_OF_FEATURES_SUCCESS, true,
            200, 0L, authenticatedUserId);

    // Collect feature IDs into a Set<Long>
    Set<FeatureDTO> featureDTOs = featureList.stream()
            .map(feature -> new FeatureDTO(feature.getId(),
                    feature.getFeatureName(),
                    feature.getFeatureDescription(),
                    true))
            .collect(Collectors.toSet());

    return new FeatureResponseDto(featureDTOs, permissions);
  }

  public List<FeatureDTO> getAllFeaturesWithStatus(Long tenantId,
                                                   InsightsFrontendDto insightsData) {
    Long authenticatedUserId = (Long) authenticationDetailUtil
            .getAuthenticationDetails("id");

    TenantInsightDto tenantInsight = TenantInsightDto.builder()
            .insightsDataFrontend(insightsData)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .GET_TENANT_FEATURES_WITH_STATUS_REQUEST_RECEIVED, tenantInsight,
            InsightsMessages
                    .GET_ALL_FEATURES_WITH_STATUS_RECEIVED, true,
            202, 0L, authenticatedUserId);

    List<Feature> allFeatures = featureRepository.findAll();

    Set<String> allowedFeatures = Set.of(
            "Home", "Subscription", "Tenants", "All Users",
            "Tenant User Tickets", "Tenant Admin Tickets",
            "Roles And Access", "Settings", "Insights"
    );


    List<TenantFeature> tenantFeatures =
            tenantFeatureRepository.findByTenantId(tenantId);

    Set<Long> enabledFeatureIds = tenantFeatures.stream()
      .map(TenantFeature::getFeatureId)
      .collect(Collectors.toSet());

    // Create a list of FeatureDTO to return with enabled/disabled flags
    List<FeatureDTO> featureDTOs = allFeatures.stream()
            .filter(feature -> allowedFeatures.contains(feature.getFeatureName()))
            .map(feature -> new FeatureDTO(feature.getId(),
                    feature.getFeatureName(),
                    feature.getFeatureDescription(),
                    enabledFeatureIds.contains(feature.getId())))
            .sorted(Comparator.comparing(FeatureDTO::getId))
            .collect(Collectors.toList());

    insightEventService.sendEvents(InsightsEventId
                    .GET_TENANT_FEATURES_WITH_STATUS_SUCCESS, tenantInsight,
            InsightsMessages.GET_ALL_FEATURES_WITH_STATUS_SUCCESS, true,
            200, 0L, authenticatedUserId);

    return featureDTOs;
  }

  @Transactional
  public void addFeatureToTenant(
          Long tenantId, Long featureId, InsightsFrontendDto insightData
  ) {
    Long authenticatedUserId =
            (Long) authenticationDetailUtil
                    .getAuthenticationDetails("id");

    TenantInsightDto tenantInsight = TenantInsightDto.builder()
            .insightsDataFrontend(insightData)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .ENABLE_TOGGLING_ON_FEATURE_RECEIVED, tenantInsight,
            InsightsMessages.TOGGLE_FEATURE_ON_RECEIVED,
            true, 202, 0L, authenticatedUserId);

    Tenant tenant = tenantRepository.findById(tenantId)
      .orElseThrow(() -> {
        insightEventService.sendEvents(InsightsEventId
                        .ENABLE_TOGGLING_ON_FEATURE_FAILURE, tenantInsight,
                InsightsMessages.TENANT_NOT_FOUND, false,
                404, 0L, authenticatedUserId);

        throw new ResourceNotFoundException(
                String.format("Tenant not found with id: %s", tenantId)
        );
      });

    Feature feature = featureRepository
            .findById(featureId)
      .orElseThrow(() -> {
        insightEventService.sendEvents(InsightsEventId
                        .ENABLE_TOGGLING_ON_FEATURE_FAILURE, tenantInsight,
                InsightsMessages.FEATURE_NOT_FOUND, false,
                404, 0L, authenticatedUserId);

        throw new ResourceNotFoundException(
                String.format("Feature not found with id: %s", featureId));
      });

    tenant.getFeatures().add(feature);
    tenantRepository.save(tenant);

    insightEventService.sendEvents(InsightsEventId
                    .ENABLE_TOGGLING_ON_FEATURE_SUCCESS, tenantInsight,
            InsightsMessages.TOGGLE_FEATURE_ON_SUCCESS, true,
            200, 0L, authenticatedUserId);

  }

  @Transactional
  public void removeFeatureFromTenant(
          Long tenantId, Long featureId, InsightsFrontendDto insightData
  ) {
    Long authenticatedUserId =
            (Long) authenticationDetailUtil
                    .getAuthenticationDetails("id");

    TenantInsightDto tenantInsight = TenantInsightDto.builder()
            .insightsDataFrontend(insightData)
            .build();

    insightEventService.sendEvents(InsightsEventId
                    .DISABLE_TOGGLING_OFF_FEATURE_RECEIVED, tenantInsight,
            InsightsMessages.TOGGLE_FEATURE_OFF_RECEIVED, true,
            202, 0L, authenticatedUserId);

    // Check if mapping exists before deletion
    if (tenantFeatureRepository
            .existsByTenantIdAndFeatureId(tenantId, featureId)) {
      tenantFeatureRepository
              .deleteByTenantIdAndFeatureId(tenantId, featureId);
      insightEventService
              .sendEvents(InsightsEventId.DISABLE_TOGGLING_OFF_FEATURE_SUCCESS,
                      tenantInsight,
              InsightsMessages.TOGGLE_FEATURE_OFF_SUCCESS, true, 200, 0L, authenticatedUserId);

    } else {
      insightEventService.sendEvents(InsightsEventId
                      .DISABLE_TOGGLING_OFF_FEATURE_FAILURE, tenantInsight,
              InsightsMessages.TENANT_NOT_FOUND, false, 404,
              0L, authenticatedUserId);
      throw new ResourceNotFoundException(
              "Mapping not found for Tenant ID: "
        + tenantId + " and Feature ID: " + featureId);
    }
  }
}
