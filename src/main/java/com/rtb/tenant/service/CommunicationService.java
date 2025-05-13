package com.rtb.tenant.service;

import com.rtb.tenant.dto.RequestBodyForEmail;
import com.rtb.tenant.dto.RequestBodyForFile;
import com.rtb.tenant.utls.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Service
public class CommunicationService {

  @Value("${external.api.file-service.url}")
  private String fileServiceUrl;

  @Value("${external.api.communication-service.url}")
  private String communicationServiceUrl;

  public String sendEmail(
      Long tenantId,
      String folderName,
      String fileName,
      List<String> placeHolders,
      String receiverEmail,
      String subject
  ) {

    String emailBody = getTemplateFromS3(tenantId, folderName, fileName, placeHolders);

    // send email using communication service
    RestTemplate restTemplate = new RestTemplate();

    RequestBodyForEmail requestBody = new RequestBodyForEmail(
        receiverEmail,
        subject,
        emailBody
    );

    HttpEntity<RequestBodyForEmail> request = new HttpEntity<>(requestBody);

    ResponseEntity<String> response = restTemplate.postForEntity(
        communicationServiceUrl,
        request,
        String.class
    );

    if (response.getStatusCode().equals(HttpStatus.OK)) {
      return response.getBody();
    } else {
      return Constants.ERROR_SENDING_MAIL;
    }
  }

  private String getTemplateFromS3(
      Long tenantId,
      String folder,
      String filename,
      List<String> placeHolders
  ) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<RequestBodyForFile> entity = new HttpEntity<>(headers);

    ResponseEntity<?> response = restTemplate.exchange(
        fileServiceUrl + "/api/v1/download"
            + "?tenantId=" + tenantId
            + "&folderName=" + folder
            + "&fileName=" + filename,
        HttpMethod.GET,
        entity,
        byte[].class
    );

    MediaType contentType = response.getHeaders().getContentType();

    if (!Objects.isNull(contentType)) {
      if (MediaType.TEXT_PLAIN.equals(contentType)) {
        byte[] value = (byte[]) response.getBody();
        String fileText = new String(value, StandardCharsets.UTF_8);
        for (String placeHolder: placeHolders) {
          fileText = fileText.replaceFirst("\\{value}", placeHolder);
        }
        return fileText;
      } else {
        throw new RuntimeException("File is not of type text/plain");
      }
    } else {
      return "Content type is not present in the response.";
    }
  }
}
