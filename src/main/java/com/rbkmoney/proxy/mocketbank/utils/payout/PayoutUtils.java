package com.rbkmoney.proxy.mocketbank.utils.payout;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PayoutUtils {

    public static Optional<CardPayout> extractCardPayoutByPan(List<CardPayout> cardList, String pan) {
        return cardList.stream().filter(card -> card.getPan().equals(pan)).findFirst();
    }
}
