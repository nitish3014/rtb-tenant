package com.rtb.tenant.utls;

public final class Constants {

  private Constants() {}

  // S3 folder names
  public static final String TENANT_TEMPLATE_FOLDER = "tenant-template";

  public static final String TENANT_LOGO_FOLDER = "user-logo";

  public static final String TENANT_CSV_FOLDER = "tenant-csv";

  public static final String TENANT_FAQ_FOLDER = "tenant-faq";

  // Exception messages
  public static final String INVALID_CHANNEL = "The channel is invalid";

  public static final String ERROR_SENDING_MAIL = "Error sending mail";

  // Channels
  public static final String SMS = "sms";

  public static final String EMAIL = "email";

  public static final String PUSH = "push";

  // TenantService Return Messages
  public static final String CHANGE_COMMUNICATION_STATUS_MESSAGE =
      "Changed the communication status to: ";

  // TenantService Exception Messages
  public static final String TENANT_COMMUNICATION_NOT_FOUND =
      "Such Tenant Communication was not found!";

  public static final String TENANT_COMMUNICATION_IS_ALREADY =
      "Tenant Communication is in same state";

  public static final String ILLEGAL_COMMUNICATION_STATUS = "Communication Status is invalid";

  public static final String TENANT_NOT_FOUND =
      "Tenant with that Id was not found";

  // Communication Status
  public static final String ACTIVE = "ACTIVE";

  public static final String INACTIVE = "INACTIVE";

}
