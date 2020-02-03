package com.rbkmoney.proxy.mocketbank.utils.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardUtils {
    public static Optional<Card> extractCardByPan(List<Card> cardList, String pan) {
        return cardList.stream().filter(card -> card.getPan().equals(pan)).findFirst();
    }
}
