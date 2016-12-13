package com.rbkmoney.proxy.test.utils.model;


public class Card {

    private String pan;

    private String action;

    private String paymentSystem;

    public Card(String pan, String action, String paymentSystem) {
        this.pan = pan;
        this.action = action;
        this.paymentSystem = paymentSystem;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPaymentSystem() {
        return paymentSystem;
    }

    public void setPaymentSystem(String paymentSystem) {
        this.paymentSystem = paymentSystem;
    }

}
