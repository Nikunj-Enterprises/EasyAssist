package com.easyapper.bloodline;

public enum BloodGroup {
    A1("A+ve"),A2("A-ve"),O1("O+ve"), O2("O-ve"), B1("B+ve"), B2("B-ve"), AB1("AB+ve"), AB("AB-ve");

    private final String value;
    BloodGroup(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}