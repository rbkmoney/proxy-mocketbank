package com.rbkmoney.proxy.mocketbank.extractor;

import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import com.rbkmoney.damsel.cds.CardData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankCardExtractor {

    private static final String UNKNOWN = "UNKNOWN";

    public static CardDataProxyModel initCardDataProxyModel(CardData cardData) {
        String cardHolder = UNKNOWN;
        if (cardData.getCardholderName() != null) {
            cardHolder = cardData.getCardholderName();
        }

        byte expMonth = Byte.parseByte("");
        short expYear = Short.parseShort("");
        if (cardData.isSetExpDate()) {
            expMonth = cardData.getExpDate().getMonth();
            expYear = cardData.getExpDate().getYear();
        }

        return CardDataProxyModel.builder()
                .cardholderName(cardHolder)
                .pan(cardData.getPan())
                .expMonth(expMonth)
                .expYear(expYear)
                .build();
    }


}
