package com.rbkmoney.proxy.mocketbank.utils.reader;

import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayout;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class CardPayoutReader implements BeanReader<CardPayout> {

    private static final String REGEXP = ", ";

    @Override
    public List<CardPayout> readList(InputStream is) {
        return extractListFromFile(is,
                line -> {
                    String[] p = line.split(REGEXP);
                    return new CardPayout(p[0], p[1], p[2]);
                });
    }
}
