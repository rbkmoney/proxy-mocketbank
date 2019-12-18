package com.rbkmoney.proxy.mocketbank.utils.extractor;

import com.rbkmoney.proxy.mocketbank.utils.model.Card;

import java.io.InputStream;
import java.util.List;

public class CardReader implements BeanReader<Card> {

    private static final String REGEXP = ", ";

    @Override
    public List<Card> readList(InputStream is) {
        return extractListFromFile(is,
                line -> {
                    String[] p = line.split(REGEXP);
                    return new Card(p[0], p[1], p[2]);
                });
    }
}
