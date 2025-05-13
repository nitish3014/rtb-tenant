package com.rtb.tenant.utls;


public class InsightsMessages {
    private InsightsMessages() {

    }
    public static final String CREATE_TENANT_RECEIVED
            = "Request received to Create Tenant";
    public static final String CREATE_TENANT_SUCCESS
            = "Create Tenant request Success";

    public static final String ENABLE_TENANT_RECEIVED
            = "Request Received to Enable tenant";
    public static final String ENABLE_TENANT_SUCCESS
            = "Enable Tenant Request Success";

    public static final String DISABLE_TENANT_RECEIVED
            = "Request Received to Disable tenant";
    public static final String DISABLE_TENANT_SUCCESS
            = "Disable Tenant Request Success";


    public static final String GET_ALL_TENANTS_WITH_USER_COUNT_RECEIVED
            = "Request Received to Get All Tenants partial details with user count";
    public static final String GET_ALL_TENANTS_WITH_USER_COUNT_SUCCESS
            = "Get All Tenants partial details with user count Success";


    public static final String GET_TENANT_DETAILS_BY_ID_RECEIVED
            = "Request Initialized to Get Tenant Details by id";
    public static final String GET_TENANT_DETAILS_BY_ID_SUCCESS
            = "Get Tenant Details by id Success";


    public static final String UPDATE_TENANT_DETAILS_RECEIVED
            = "Request Received to Update Tenant Details";
    public static final String UPDATE_TENANT_DETAILS_SUCCESS
            = "Update Tenant Details Success";

    public static final String UPLOAD_TENANT_LOGO_RECEIVED
            = "Request Received to Upload Tenant Logo";
    public static final String UPLOAD_TENANT_LOGO_SUCCESS
            = "Upload Tenant Logo Success";
    public static final String INTERNAL_SERVER_ERROR
            = "INTERNAL_SERVER_ERROR";
    public static final String NOT_FOUND
            = "NOT_FOUND";


    public static final String UPLOAD_TEMPLATE_FILE_RECEIVED
            = "Request Received to Upload Template File Request";
    public static final String UPLOAD_TEMPLATE_FILE_SUCCESS
            = "Upload Template File Request Success";


    public static final String
            CREATE_COMMUNICATION_TEMPLATE_RECEIVED
            = "Request Received to Create Communication Template";
    public static final String
            CREATE_COMMUNICATION_TEMPLATE_SUCCESS
            = "Create Communication Template Success";

    public static final String
            UPDATE_COMMUNICATION_CHANNEL_TEMPLATE_RECEIVED
            = "Request Received to Update Communication Channel Template";
    public static final String
            UPDATE_COMMUNICATION_CHANNEL_TEMPLATE_SUCCESS
            = "Update Communication Channel Template Success";

    public static final
    String UPDATE_COMMUNICATION_CHANNEL_STATE_RECEIVED
            = "Request Received to Update Communication Channel State";
    public static final
    String UPDATE_COMMUNICATION_CHANNEL_STATE_SUCCESS
            = "Update Communication Channel Success";

    public static final String
            DELETE_COMMUNICATION_CHANNEL_TEMPLATE_RECEIVED
            = "Request Received to Delete Tenant Communication Channel Template";
    public static final String
            DELETE_COMMUNICATION_CHANNEL_TEMPLATE_SUCCESS
            = "Delete Tenant Communication Channel Template Success";

    public static final String UPLOAD_CSV_RECEIVED
            = "Request Received to Upload CSV";
    public static final String UPLOAD_CSV_SUCCESS
            = "Upload CSV Success";

    public static final String
            GET_METADATA_OF_FEATURES_RECEIVED
            = "Request Received to Get Metadata of Features";
    public static final String
            GET_METADATA_OF_FEATURES_SUCCESS =
            "Get Metadata of Features Success";

    public static final String
            GET_ALL_FEATURES_WITH_STATUS_RECEIVED
            = "Request received to Get all Features with Status";
    public static final String
            GET_ALL_FEATURES_WITH_STATUS_SUCCESS
            = "Get all Features with Status Success";


    public static final String
            TOGGLE_FEATURE_ON_RECEIVED =
            "Request Received to Toggle On the feature";
    public static final String
            TOGGLE_FEATURE_ON_SUCCESS =
            "Toggle On the feature Success";
    public static final String
            TENANT_NOT_FOUND =
            "TENANT_NOT_FOUND";
    public static final String
            FEATURE_NOT_FOUND =
            "FEATURE_NOT_FOUND";


    public static final String
            TOGGLE_FEATURE_OFF_RECEIVED =
            "Request received to Toggle Off the Feature";
    public static final String
            TOGGLE_FEATURE_OFF_SUCCESS =
            "Toggle Off the Feature Success";

    public static final String
            SAVE_FAILED =
            "Failed to save enabled Tenant";
    public static final String
            UNEXPECTED_ERROR =
            "An unexpected error occurred while enabling the tenant";
    public static final String
            COMMUNICATION_CHANNEL_NOT_FOUND =
            "Communication Channel not found";
    public static final String TEMPLATE_ALREADY_EXISTS =
            "Template already exists for the tenant with category";
}
