package com.certificationschedular.demo.model;

public class DeviceDetail {
    private String deviceId;
    private String c1;
    private String c2;
    private String c3;
    private long validationStartTime=0;
    private long validationEndTime=0;

    public long getValidationStartTime() {
        return validationStartTime;
    }

    public void setValidationStartTime(long validationStartTime) {
        this.validationStartTime = validationStartTime;
    }

    public long getValidationEndTime() {
        return validationEndTime;
    }

    public void setValidationEndTime(long validationEndTime) {
        this.validationEndTime = validationEndTime;
    }

    public boolean isCertificationValidationStatus() {
        return certificationValidationStatus;
    }

    public void setCertificationValidationStatus(boolean certificationValidationStatus) {
        this.certificationValidationStatus = certificationValidationStatus;
    }

    private boolean certificationValidationStatus;


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getC1() {
        return c1;
    }

    public void setC1(String c1) {
        this.c1 = c1;
    }

    public String getC2() {
        return c2;
    }

    public void setC2(String c2) {
        this.c2 = c2;
    }

    public String getC3() {
        return c3;
    }

    public void setC3(String c3) {
        this.c3 = c3;
    }

    @Override
    public String toString() {
        return "DeviceDetail{" +
                "deviceId='" + deviceId + '\'' +
                ", c1='" + c1 + '\'' +
                ", c2='" + c2 + '\'' +
                ", c3='" + c3 + '\'' +
                ", certificationValidationStatus=" + certificationValidationStatus +
                ", validationStartTime=" + validationStartTime +
                ", validationEndTime=" + validationEndTime +
                ", responseTime=" + (validationEndTime - validationStartTime) + " ms" +
                '}';
    }
}
