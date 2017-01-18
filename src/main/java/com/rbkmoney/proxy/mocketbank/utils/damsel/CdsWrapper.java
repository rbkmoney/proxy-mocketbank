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

    public static PutCardDataResult makePutCardDataResult(BankCard bankCard, String session) {
        PutCardDataResult putCardDataResult = new PutCardDataResult();
        putCardDataResult.setBankCard(bankCard);
        putCardDataResult.setSession(session);
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
