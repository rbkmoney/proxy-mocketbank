package com.rbkmoney.proxy.mocketbank.utils.mocketbank;


public interface MocketBankMpi {

    public Object verifyEnrollment(String pan, short year, byte month);

    public Object validatePaRes();

}
