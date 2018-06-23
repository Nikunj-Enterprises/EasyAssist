package com.easyapper.authservice.util;

public class UserValidationRequest {
    private String userId;
    private String requiredRoleName;
    private String token;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRequiredRoleName() {
        return requiredRoleName;
    }

    public void setRequiredRoleName(String requiredRoleName) {
        this.requiredRoleName = requiredRoleName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
