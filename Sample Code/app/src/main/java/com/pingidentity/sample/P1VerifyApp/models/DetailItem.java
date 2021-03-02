package com.pingidentity.sample.P1VerifyApp.models;

public class DetailItem {

    private String paramName;
    private String paramInfo;
    private boolean isUnderline;

    public DetailItem(String paramName, String paramInfo, boolean isUnderline) {
        this.paramName = paramName;
        this.paramInfo = paramInfo;
        this.isUnderline = isUnderline;
    }

    public String getParamName() {
        return paramName;
    }

    public String getParamInfo() {
        return paramInfo;
    }

    public boolean isUnderline() {
        return isUnderline;
    }
}
