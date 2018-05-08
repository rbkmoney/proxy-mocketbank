package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.domain.BankCard;


public class CdsWrapper {

    public static ExpDate makeExpDate(byte month, short year) {
        ExpDate expDate = new ExpDate();
        expDate.setMonth(month);
        expDate.setYear(year);
        return expDate;
    }

    public static CardData makeCardData(String cardholderName, String cvv, String pan, ExpDate expDate) {
        CardData cardData = new CardData();
        cardData.setCardholderName(cardholderName);
        cardData.setCvv(cvv);
        cardData.setPan(pan);
        cardData.setExpDate(expDate);
        return cardData;
    }

    public static CardData makeCardDataWithExpDate(String cardholderName, String cvv, String pan, byte month, short year) {
        return CdsWrapper.makeCardData(cardholderName, cvv, pan, CdsWrapper.makeExpDate(month, year));
    }

    public static SessionData makeSessionData(AuthData authData) {
        SessionData sessionData = new SessionData();
        sessionData.setAuthData(authData);
        return sessionData;
    }

    public static AuthData makeAuthData(Auth3DS auth3DS, CardSecurityCode cardSecurityCode) {
        AuthData authData = new AuthData();
        authData.setAuth3ds(auth3DS);
        authData.setCardSecurityCode(cardSecurityCode);
        return authData;
    }

    public static AuthData makeAuthData(CardSecurityCode cardSecurityCode) {
        AuthData authData = new AuthData();
        authData.setCardSecurityCode(cardSecurityCode);
        return authData;
    }

    public static AuthData makeAuthData(Auth3DS auth3DS) {
        AuthData authData = new AuthData();
        authData.setAuth3ds(auth3DS);
        return authData;
    }

    public static AuthData makeAuthData(Auth3DS auth3DS, String cvv) {
        AuthData authData = new AuthData();
        authData.setAuth3ds(auth3DS);
        authData.setCardSecurityCode(makeCardSecurityCode(cvv));
        return authData;
    }

    public static CardSecurityCode makeCardSecurityCode(String cvv) {
        CardSecurityCode cardSecurityCode = new CardSecurityCode();
        cardSecurityCode.setValue(cvv);
        return cardSecurityCode;
    }

    public static Auth3DS makeAuth3DS(String cryptogram, String eci) {
        Auth3DS auth3DS = new Auth3DS();
        auth3DS.setCryptogram(cryptogram);
        auth3DS.setEci(eci);
        return auth3DS;
    }

    public static Auth3DS makeAuth3DS(String cryptogram) {
        return makeAuth3DS(cryptogram, null);
    }

    public static PutCardDataResult makePutCardDataResult(BankCard bankCard, String session) {
        PutCardDataResult putCardDataResult = new PutCardDataResult();
        putCardDataResult.setBankCard(bankCard);
        putCardDataResult.setSessionId(session);
        return putCardDataResult;
    }

    public static UnlockStatus makeUnlockStatusUnlocked() {
        UnlockStatus unlockStatus = new UnlockStatus();
        unlockStatus.setUnlocked(new Unlocked());
        return unlockStatus;
    }

    public static UnlockStatus makeUnlockStatusMoreKeysNeeded(short value) {
        UnlockStatus unlockStatus = new UnlockStatus();
        unlockStatus.setMoreKeysNeeded(value);
        return unlockStatus;
    }

}
