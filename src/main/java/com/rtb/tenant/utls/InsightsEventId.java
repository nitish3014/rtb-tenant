package com.rtb.tenant.utls;

public class InsightsEventId {

    private InsightsEventId() {

    }

    public static final Long CREATE_TENANT_REQUEST_RECEIVED = 8002L;
    public static final Long CREATE_TENANT_SUCCESS = 8003L;
    public static final Long CREATE_TENANT_FAILURE = 8004L;


    public static final Long ENABLE_TENANT_REQUEST_RECEIVED = 8006L;
    public static final Long ENABLE_TENANT_SUCCESS = 8007L;
    public static final Long ENABLE_TENANT_FAILURE = 8008L;


    public static final Long DISABLE_TENANT_REQUEST_RECEIVED = 8010L;
    public static final Long DISABLE_TENANT_SUCCESS = 8011L;
    public static final Long DISABLE_TENANT_FAILURE = 8012L;


    public static final Long GET_ALL_TENANTS_REQUEST_RECEIVED = 8014L;
    public static final Long GET_ALL_TENANTS_SUCCESS = 8015L;
    public static final Long GET_ALL_TENANTS_FAILURE = 8016L;


    public static final Long GET_TENANT_BY_ID_REQUEST_RECEIVED = 8018L;
    public static final Long GET_TENANT_BY_ID_SUCCESS = 8019L;
    public static final Long GET_TENANT_BY_ID_FAILURE = 8020L;


    public static final Long UPDATE_TENANT_REQUEST_RECEIVED = 8022L;
    public static final Long UPDATE_TENANT_SUCCESS = 8023L;
    public static final Long UPDATE_TENANT_FAILURE = 8024L;


    public static final Long UPDATE_TENANT_LOGO_REQUEST_RECEIVED = 8026L;
    public static final Long UPDATE_TENANT_LOGO_SUCCESS = 8027L;
    public static final Long UPDATE_TENANT_LOGO_FAILURE = 8028L;


    public static final Long UPLOAD_COMMUNICATION_TEMPLATE_REQUEST_RECEIVED = 8030L;
    public static final Long UPLOAD_COMMUNICATION_TEMPLATE_SUCCESS = 8031L;
    public static final Long UPLOAD_COMMUNICATION_TEMPLATE_FAILURE = 8032L;


    public static final Long CREATE_COMMUNICATION_TEMPLATE_REQUEST_RECEIVED = 8034L;
    public static final Long CREATE_COMMUNICATION_TEMPLATE_SUCCESS = 8035L;
    public static final Long CREATE_COMMUNICATION_TEMPLATE_FAILURE = 8036L;


    public static final Long UPDATE_COMMUNICATION_TEMPLATE_REQUEST_RECEIVED = 8038L;
    public static final Long UPDATE_COMMUNICATION_TEMPLATE_SUCCESS = 8039L;
    public static final Long UPDATE_COMMUNICATION_TEMPLATE_FAILURE = 8040L;


    public static final Long UPDATE_COMMUNICATION_CHANNEL_STATUS_REQUEST_RECEIVED = 8042L;
    public static final Long UPDATE_COMMUNICATION_CHANNEL_STATUS_SUCCESS = 8043L;
    public static final Long UPDATE_COMMUNICATION_CHANNEL_STATUS_FAILURE = 8044L;


    public static final Long DELETE_COMMUNICATION_CHANNEL_TEMPLATE_REQUEST_RECEIVED = 8046L;
    public static final Long DELETE_COMMUNICATION_CHANNEL_TEMPLATE_SUCCESS = 8047L;
    public static final Long DELETE_COMMUNICATION_CHANNEL_TEMPLATE_FAILURE = 8048L;


    public static final Long UPLOAD_CSV_FOR_TENANT_REQUEST_RECEIVED = 8050L;
    public static final Long UPLOAD_CSV_FOR_TENANT_SUCCESS = 8051L;
    public static final Long UPLOAD_CSV_FOR_TENANT_FAILURE = 8052L;


    public static final Long GET_METADATA_OF_FEATURES_REQUEST_RECEIVED = 8054L;
    public static final Long GET_METADATA_OF_FEATURES_SUCCESS = 8055L;
    public static final Long GET_METADATA_OF_FEATURES_FAILURE = 8056L;


    public static final Long GET_TENANT_FEATURES_WITH_STATUS_REQUEST_RECEIVED = 8058L;
    public static final Long GET_TENANT_FEATURES_WITH_STATUS_SUCCESS = 8059L;


    public static final Long ENABLE_TOGGLING_ON_FEATURE_RECEIVED = 8062L;
    public static final Long ENABLE_TOGGLING_ON_FEATURE_SUCCESS = 8063L;
    public static final Long ENABLE_TOGGLING_ON_FEATURE_FAILURE = 8064L;

    public static final Long DISABLE_TOGGLING_OFF_FEATURE_RECEIVED = 8066L;
    public static final Long DISABLE_TOGGLING_OFF_FEATURE_SUCCESS = 8067L;
    public static final Long DISABLE_TOGGLING_OFF_FEATURE_FAILURE = 8068L;

}
