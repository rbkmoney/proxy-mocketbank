package com.rbkmoney.proxy.test.utils.testmpi.model;


public class VerifyEnrollmentRequest {

    private String pan;
    private String year;
    private String month;

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @Override
    public String toString() {
        return "VerifyEnrollmentRequest{" +
                "pan='" + pan + '\'' +
                ", year='" + year + '\'' +
                ", month='" + month + '\'' +
                '}';
    }
}
