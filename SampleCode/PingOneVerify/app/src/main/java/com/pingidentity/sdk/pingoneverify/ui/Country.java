package com.pingidentity.sdk.pingoneverify.ui;

public class Country {

    private String name;
    private String dialCode;
    private String code;

    public Country(String name, String dialCode, String code) {
        this.name = name;
        this.dialCode = dialCode;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public String getDialCode() {
        return dialCode;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return name + "  " + dialCode;
    }

    @Override
    public String toString() {
        return "Country{" +
                "name='" + name + '\'' +
                ", dialCode='" + dialCode + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
