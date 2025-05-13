package com.rtb.tenant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rtb.tenant.dto.KafkaProduceEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


    @Service
    @Slf4j
    public class InsightEventService {
        @Autowired
        private UUIDService uuidService;

        @Autowired
        private ObjectMapper objectMapper;


        @Autowired
        private HttpRequestService httpRequestService;

        private final String messageBaseURL;

        public InsightEventService(@Value("${url.message_bus_service}") String messageBaseURL) {
            this.messageBaseURL = messageBaseURL;
        }

        public void sendEvents(Long eventId, Object data,
                               String message, Boolean status,
                               Integer httpStatusCode, Long tenantId, Long userId) {

            ObjectNode objectNodeData = objectMapper.convertValue(data, ObjectNode.class);
            if (objectNodeData.get("insightsDataFrontend") == null) {
                objectNodeData.put("insightsDataFrontend", objectMapper.createObjectNode());
            }

            KafkaProduceEventDto kafkaProduceEventDto =
                    getParsedKafkaProduceEventDto(eventId, objectNodeData,
                    message, status, httpStatusCode, tenantId, userId);
            sendDataToKafka(kafkaProduceEventDto, tenantId);
        }

        private KafkaProduceEventDto getParsedKafkaProduceEventDto(Long eventId, ObjectNode data,
                                                                   String message, Boolean status,
                                                                   Integer httpStatusCode,
                                                                   Long tenantId, Long userId) {
            ObjectNode extendedPayload = objectMapper.createObjectNode();
            extendedPayload.put("IP", data.get("insightsDataFrontend").get("ipAddress"));
            extendedPayload.put("Location", data.get("insightsDataFrontend").get("location"));
            extendedPayload.put("Timezone", data.get("insightsDataFrontend").get("timeZone"));
            extendedPayload.put("Status", status);
            extendedPayload.put("HttpStatusCode", httpStatusCode);
            extendedPayload.put("Message", message);
            extendedPayload.put("AndroidId", data.get("insightsDataFrontend").get("androidId"));
            extendedPayload.put("AppleId", data.get("insightsDataFrontend").get("appleId"));
            extendedPayload.put("DeviceBrand", data.get("insightsDataFrontend").get("deviceBrand"));
            extendedPayload.put("DeviceModel", data.get("insightsDataFrontend").get("deviceModel"));
            extendedPayload.put("AppVersion", data.get("insightsDataFrontend").get("appVersion"));
            extendedPayload.put("OSVersion", data.get("insightsDataFrontend").get("osVersion"));
            extendedPayload.put("NetworkProvider",
                    data.get("insightsDataFrontend").get("networkProvider"));


            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("RecordId", uuidService.generateRandomUUID());
            payload.put("EventId", eventId);
            payload.put("EventTimestamp", System.currentTimeMillis());
            payload.put("TenantId", tenantId);
            payload.put("UserId", userId);
            if (data.has("communicationId") && !data.get("communicationId").isNull()) {
                payload.put("communicationId", data.get("communicationId").asLong());
            }
            payload.set("ExtendedPayload", extendedPayload);
            System.out.println(payload);

            return KafkaProduceEventDto.builder()
                    .eventname(AppConstants.ENTITY_NAME)
                    .origin(AppConstants.ENTITY_ID)
                    .timestamp(System.currentTimeMillis())
                    .payload(payload)
                    .build();
        }

        private void sendDataToKafka(
                KafkaProduceEventDto kafkaProduceEventDto, Long tenantId
        ) {

            Map<String, Object> reqBody = objectMapper
                    .convertValue(kafkaProduceEventDto, Map.class);

            String endpointUrl = String.format("%s/api/v1/messagebus/event/1",
                    messageBaseURL.trim());

            try {
                httpRequestService.sendPostRequest(endpointUrl, reqBody);
            } catch (IOException | InterruptedException e) {
                log.error("Error sending data to kafka", e);
            }
        }
    }

