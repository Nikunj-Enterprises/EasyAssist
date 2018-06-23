package com.easyapper.gateway.util;

public class UserAuthenticationResponse {
    private String contextId;
    private Long contextIdExpiryMillis;

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Long getContextIdExpiryMillis() {
        return contextIdExpiryMillis;
    }

    public void setContextIdExpiryMillis(Long contextIdExpiryMillis) {
        this.contextIdExpiryMillis = contextIdExpiryMillis;
    }
}
