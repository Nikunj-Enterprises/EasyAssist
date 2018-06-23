package com.easyapper.authservice.common;

public class AppContext {
    private String appId;

    public AppContext(String appId){
        this.appId = appId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
