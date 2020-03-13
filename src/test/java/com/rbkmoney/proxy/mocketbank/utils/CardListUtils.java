package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardListUtils {

    public static List<String> extractPans(List<Card> cardList, Predicate<Card> cardPredicate) {
        return cardList.stream()
                .filter(cardPredicate)
                .map(Card::getPan)
                .collect(Collectors.toList());
    }

}
