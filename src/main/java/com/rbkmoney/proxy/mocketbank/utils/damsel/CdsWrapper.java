package com.rbkmoney.proxy.mocketbank.utils.damsel;

import com.rbkmoney.damsel.cds.*;
import com.rbkmoney.damsel.domain.BankCard;


public class CdsWrapper {

    public static final String DEFAULT_CARDHOLDER_NAME = "NONAME";

    public static ExpDate makeExpDate(byte month, short year) {
        return new ExpDate(month, year);
    }

    public static ExpDate makeExpDate(String month, String year) {
        return makeExpDate(Byte.parseByte(month), Short.parseShort(year));
    }

    public static CardData makeCardData(String cardholderName, String cvv, String pan, ExpDate expDate) {
        return new CardData(pan, expDate).setCardholderName(cardholderName).setCvv(cvv);
    }

    public static CardData makeCardDataWithExpDate(String cardholderName, String cvv, String pan, byte month, short year) {
        return makeCardData(cardholderName, cvv, pan, makeExpDate(month, year));
    }

    public static CardData makeCardDataWithExpDate(String cardholderName, String cvv, String pan, String month, String year) {
        return makeCardData(cardholderName, cvv, pan, makeExpDate(month, year));
    }

    public static SessionData makeSessionData(AuthData authData) {
        return new SessionData(authData);
    }

    public static AuthData makeAuthData(CardSecurityCode cardSecurityCode) {
        return AuthData.card_security_code(cardSecurityCode);
    }

    public static AuthData makeAuthDataWithAuth3DS(Auth3DS auth3DS) {
        return AuthData.auth_3ds(auth3DS);
    }

    public static CardSecurityCode makeCardSecurityCode(String cvv) {
        return new CardSecurityCode(cvv);
    }

    public static Auth3DS makeAuth3DS(String cryptogram, String eci) {
        return new Auth3DS(cryptogram).setEci(eci);
    }

    public static AuthData makeAuthDataWithCryptogramAndEci(String cryptogram, String eci) {
        return AuthData.auth_3ds(makeAuth3DS(cryptogram, eci));
    }

    public static AuthData makeAuthDataWithCardSecurityCode(String cvv) {
        return AuthData.card_security_code(makeCardSecurityCode(cvv));
    }

    public static Auth3DS makeAuth3DS(String cryptogram) {
        return makeAuth3DS(cryptogram, null);
    }

    public static PutCardDataResult makePutCardDataResult(BankCard bankCard, String session) {
        return new PutCardDataResult(bankCard, session);
    }

    public static UnlockStatus makeUnlockStatusUnlocked() {
        return UnlockStatus.unlocked(new Unlocked());
    }

    public static UnlockStatus makeUnlockStatusMoreKeysNeeded(short value) {
        return UnlockStatus.more_keys_needed(value);
    }

}
