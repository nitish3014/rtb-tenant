package com.rtb.tenant.service;

import com.rtb.tenant.dto.Tenant_Insights.Request.InsightsFrontendDto;
import com.rtb.tenant.dto.Tenant_Insights.Request.TenantInsightDto;
import com.rtb.core.entity.tenant.Tenant;
import com.rtb.tenant.exception.InternalServerErrorException;
import com.rtb.tenant.exception.IllegalArgumentException;
import com.rtb.tenant.exception.ResourceNotFoundException;
import com.rtb.core.repository.TenantRepository;
import com.rtb.tenant.utls.AuthenticationDetailUtil;
import com.rtb.tenant.utls.InsightsEventId;
import com.rtb.tenant.utls.InsightsMessages;
import com.rtb.tenant.utls.Constants;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;


@Service
public class S3FileService {

    private final TenantRepository tenantRepository;
    private final InsightEventService insightEventService;
    private final AuthenticationDetailUtil authenticationDetailUtil;
    @Value("${external.api.file-service.url}")
    private String fileServiceUrl;

    public S3FileService(
        RestTemplate restTemplate,
        TenantRepository tenantRepository,
        InsightEventService insightEventService,
        AuthenticationDetailUtil authenticationDetailUtil
    ) {
        this.tenantRepository = tenantRepository;
        this.insightEventService = insightEventService;
        this.authenticationDetailUtil = authenticationDetailUtil;
    }

    public String uploadTemplate(Long tenantId,
                                 MultipartFile file,
                                 InsightsFrontendDto insightsFrontendDto
    ) throws IOException {
        Long authenticatedTenantId =
                (Long) authenticationDetailUtil.getAuthenticationDetails("tenantid");
        Long authenticatedUserId =
                (Long) authenticationDetailUtil.getAuthenticationDetails("id");

        TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
                .insightsDataFrontend(insightsFrontendDto)
                .build();

        insightEventService.sendEvents(InsightsEventId
                        .UPLOAD_COMMUNICATION_TEMPLATE_REQUEST_RECEIVED,
                tenantInsightDto,
                InsightsMessages.UPLOAD_TEMPLATE_FILE_RECEIVED, true,
                202, 0L, authenticatedUserId);

        String userLogoFolder = Constants.TENANT_LOGO_FOLDER;

        ResponseEntity<String> response = uploadToS3Template
                .apply(tenantId, userLogoFolder, file);

        if (response.getStatusCode() == HttpStatus.OK) {
            String logoUrl = response.getBody();

            insightEventService.sendEvents(InsightsEventId
                            .UPLOAD_COMMUNICATION_TEMPLATE_SUCCESS, tenantInsightDto,
                    InsightsMessages.UPLOAD_TEMPLATE_FILE_SUCCESS, true,
                    201, 0L, authenticatedUserId);

            return updateTenantLogo(tenantId, logoUrl);
        } else {
            insightEventService.sendEvents(InsightsEventId
                            .UPLOAD_COMMUNICATION_TEMPLATE_FAILURE, tenantInsightDto,
                    InsightsMessages.UNEXPECTED_ERROR, true,
                    500, 0L, authenticatedUserId);

            throw new InternalServerErrorException("Failed to upload file: "
                    + response.getStatusCode());
        }
    }

    public String uploadFileAndSaveUrl(Long tenantId,
                                       MultipartFile file, InsightsFrontendDto insightsFrontendDto
    ) throws IOException {
        Long authenticatedTenantId
                = (Long) authenticationDetailUtil.getAuthenticationDetails("tenantid");
        Long authenticatedUserId
                = (Long) authenticationDetailUtil
                .getAuthenticationDetails("id");

        TenantInsightDto tenantInsightDto = TenantInsightDto.builder()
                .insightsDataFrontend(insightsFrontendDto)
                .build();

        insightEventService.sendEvents(InsightsEventId
                        .UPDATE_TENANT_LOGO_REQUEST_RECEIVED, tenantInsightDto,
                InsightsMessages.UPLOAD_TENANT_LOGO_RECEIVED, true,
                202, 0L, authenticatedUserId);

        String userLogoFolder = Constants.TENANT_LOGO_FOLDER;

        ResponseEntity<String> response = uploadToS3Template.apply(tenantId, userLogoFolder, file);

        if (response.getStatusCode() == HttpStatus.OK) {
            String logoUrl = response.getBody();

            insightEventService.sendEvents(InsightsEventId
                            .UPDATE_TENANT_LOGO_SUCCESS, tenantInsightDto,
                    InsightsMessages.UPLOAD_TENANT_LOGO_SUCCESS, true,
                    201, 0L, authenticatedUserId);

            return updateTenantLogo(tenantId, logoUrl);
        } else {
            insightEventService.sendEvents(InsightsEventId
                            .UPDATE_TENANT_LOGO_FAILURE, tenantInsightDto,
                    InsightsMessages.UNEXPECTED_ERROR, true, 500,
                    0L, authenticatedUserId);

            throw new InternalServerErrorException("Failed to upload file: "
                    + response.getStatusCode());
        }
    }

    public String uploadTenantTemplate(Long tenantId,
                                       MultipartFile file) {

        String tenantTemplateFolder = Constants.TENANT_TEMPLATE_FOLDER;

        ResponseEntity<String> response = uploadToS3Template.apply(
            tenantId, tenantTemplateFolder, file
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new InternalServerErrorException(
                    "Failed to upload file: " + response.getStatusCode());
        }
    }

    public String uploadCSV(Long tenantId, MultipartFile file,
                            InsightsFrontendDto insightData) {
        Long authenticatedUserId = (Long) authenticationDetailUtil
                .getAuthenticationDetails("id");

        TenantInsightDto tenantInsight = TenantInsightDto.builder()
                .insightsDataFrontend(insightData)
                .build();

        insightEventService.sendEvents(InsightsEventId
                        .UPLOAD_CSV_FOR_TENANT_REQUEST_RECEIVED, tenantInsight,
                InsightsMessages.UPLOAD_CSV_RECEIVED, true, 202,
                0L, authenticatedUserId);

        String tenantTemplateFolder = Constants.TENANT_CSV_FOLDER;

        ResponseEntity<String> response = uploadToS3Template.apply(
            tenantId, tenantTemplateFolder, file
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            insightEventService.sendEvents(InsightsEventId
                            .UPLOAD_CSV_FOR_TENANT_SUCCESS, tenantInsight,
                    InsightsMessages.UPLOAD_CSV_SUCCESS, true,
                    200, 0L, authenticatedUserId);
            return response.getBody();
        } else {
            insightEventService.sendEvents(InsightsEventId
                            .UPLOAD_CSV_FOR_TENANT_FAILURE, tenantInsight,
                    InsightsMessages.INTERNAL_SERVER_ERROR, false,
                    500, 0L, authenticatedUserId);

            throw new InternalServerErrorException("Filed to upload file: "
                    + response.getStatusCode());
        }

    }

    public String uploadFAQ(Long tenantId, MultipartFile file) {

        String tenantTemplateFolder = Constants.TENANT_FAQ_FOLDER;

        ResponseEntity<String> response = uploadToS3Template.apply(
            tenantId, tenantTemplateFolder, file
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new InternalServerErrorException("Filed to upload file: "
                    + response.getStatusCode());
        }

    }

    private final TriFunction<Long, String,
            MultipartFile, ResponseEntity<String>>
        uploadToS3Template = (tenantId, folderName, file) ->
    {
        String externalApiUrl =
            fileServiceUrl + "/api/v1/file/upload/" + tenantId + "/" + folderName;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-Insights-Data", "null");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        try {
            body.add("file", new FileSystemResource(convertMultiPartToFile(file)));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting file");
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.postForEntity(
                externalApiUrl, requestEntity, String.class);
    };

    private String updateTenantLogo(Long tenantId, String logoUrl) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No tenant found with the id: "
                                + tenantId));
        tenant.setLogo(logoUrl);
        tenantRepository.save(tenant);
        return logoUrl;
    }

    private File convertMultiPartToFile(
            MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/"
                +
                Objects.requireNonNull(file.getOriginalFilename()));
        try (
                FileOutputStream FILE_OUTPUT_STREAM = new FileOutputStream(convFile)
        ) {
            FILE_OUTPUT_STREAM.write(file.getBytes());
        }
        return convFile;
    }
}
