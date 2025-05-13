package com.rtb.tenant.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class KafkaProduceEventDto {

    private String eventname;
    private String origin;
    private long timestamp;
    private JsonNode payload;

    @Builder
    public KafkaProduceEventDto(String eventname, String origin, long timestamp, JsonNode payload) {
        this.eventname = eventname;
        this.origin = origin;
        this.timestamp = timestamp;
        this.payload = payload;
    }
}

