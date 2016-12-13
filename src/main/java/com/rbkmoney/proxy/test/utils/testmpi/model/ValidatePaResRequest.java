package com.rbkmoney.proxy.test.utils.testmpi.model;


public class ValidatePaResRequest {

    private String pan;
    private String paRes;

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getPaRes() {
        return paRes;
    }

    public void setPaRes(String paRes) {
        this.paRes = paRes;
    }

    @Override
    public String toString() {
        return "ValidatePaResRequest{" +
                "pan='" + pan + '\'' +
                ", paRes='" + paRes + '\'' +
                '}';
    }
}
