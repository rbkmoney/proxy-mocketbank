package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardListUtils {

    public static String[] extractPans(List<Card> cardList, Predicate<Card> cardPredicate) {
        return cardList.stream()
                .filter(cardPredicate)
                .map(Card::getPan)
                .toArray(String[]::new);
    }

}
