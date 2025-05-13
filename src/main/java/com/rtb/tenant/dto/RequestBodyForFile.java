package com.rtb.tenant.dto;

import java.util.List;

public record RequestBodyForFile(
    Long tenantId,
    String folderName,
    String fileName,
    List<String> placeHolders,
    String receiverEmail,
    String subject
) {
}
