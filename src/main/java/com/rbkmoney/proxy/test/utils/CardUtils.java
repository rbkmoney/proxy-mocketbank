package com.rbkmoney.proxy.test.utils;


import com.rbkmoney.proxy.test.utils.model.Card;
import com.rbkmoney.proxy.test.utils.testmpi.constant.TestMpiAction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CardUtils {

    List<Card> cardList = Collections.emptyList();

    public CardUtils(List<Card> cardList) {
        this.cardList = cardList;
    }

    public static List<Card> getCardListFromFile(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines().skip(1).map(mapToCard).collect(Collectors.toList());
    }

    private static Function<String, Card> mapToCard = (line) -> {
        String[] p = line.split(", ");
        return new Card(p[0], p[1], p[2]);
    };

    public Optional<Card> getCardByPan(String pan) {
        return cardList.stream().filter(card -> card.getPan().equals(pan)).findFirst();
    }

    public boolean isEnrolled(Optional<Card> card) {
        boolean result = false;
        if (card.isPresent()) {
            TestMpiAction action = TestMpiAction.findByValue(card.get().getAction());
            switch (action) {
                case THREE_D_SECURE_FAILURE:
                case THREE_D_SECURE_TIMEOUT:
                case THREE_D_SECURE_SUCCESS:
                    result = true;
                    break;
                default:
                    result = false;
            }
        }
        return result;
    }
}
