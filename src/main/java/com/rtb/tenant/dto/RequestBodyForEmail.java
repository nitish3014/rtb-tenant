package com.rtb.tenant.dto;

public record RequestBodyForEmail(
    String to,
    String subject,
    String body
) {
}
