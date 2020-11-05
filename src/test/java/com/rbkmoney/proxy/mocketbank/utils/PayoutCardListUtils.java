package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayout;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayoutCardListUtils {

    public static List<String> extractPans(List<CardPayout> cardList, Predicate<CardPayout> cardPredicate) {
        return cardList.stream()
                .filter(cardPredicate)
                .map(CardPayout::getPan)
                .collect(Collectors.toList());
    }

}
