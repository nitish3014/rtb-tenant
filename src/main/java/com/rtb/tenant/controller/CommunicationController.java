package com.rtb.tenant.controller;

import com.rtb.tenant.dto.RequestBodyForFile;
import com.rtb.tenant.service.CommunicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("api/v1/communication")
public class CommunicationController {

  private final CommunicationService communicationService;

  public CommunicationController(CommunicationService communicationService) {
    this.communicationService = communicationService;
  }

  @GetMapping("message")
  @PreAuthorize("hasAuthority('communication_read')")
  public String getMessage(
      @RequestBody RequestBodyForFile request
  ) {

    return communicationService.sendEmail(
        request.tenantId(),
        request.folderName(),
        request.fileName(),
        request.placeHolders(),
        request.receiverEmail(),
        request.subject()
    );

  }
}
