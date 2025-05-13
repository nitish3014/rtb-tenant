package com.rtb.tenant.utls;

public class ApiErrorResponse {
    private String message;
    private String error;

    public ApiErrorResponse(String message, String error) {
        this.message = message;
        this.error = error;
    }
}
