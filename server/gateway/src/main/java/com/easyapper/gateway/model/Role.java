package com.easyapper.gateway.model;

public class Role {
    private String roleName;
    
    public Role(String roleName){
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
