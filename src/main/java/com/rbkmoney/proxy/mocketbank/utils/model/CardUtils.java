package com.rbkmoney.proxy.mocketbank.utils.model;

import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.rbkmoney.proxy.mocketbank.utils.model.CardAction.UNKNOWN_FAILURE;
import static com.rbkmoney.proxy.mocketbank.utils.model.CardAction.isMpiCardFailed;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardUtils {

    public static Optional<Card> extractCardByPan(List<Card> cardList, String pan) {
        return cardList.stream().filter(card -> card.getPan().equals(pan)).findFirst();
    }

    public static CardAction extractActionFromCard(List<Card> cardList, CardDataProxyModel cardData) {
        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        CardAction action = CardAction.findByValue(card.get().getAction());
        return isMpiCardFailed(action) ? action : UNKNOWN_FAILURE;
    }
}
