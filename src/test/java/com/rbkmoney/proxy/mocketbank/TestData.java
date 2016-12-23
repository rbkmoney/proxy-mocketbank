package com.rbkmoney.proxy.mocketbank;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.proxy.mocketbank.utils.damsel.CdsWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestData {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestData.class);

    public static CardData makeCardData() {
        byte month = Byte.parseByte("12");
        short year = Short.parseShort("2020");

        LOGGER.info("Date: {}", Short.toString(year).substring(2));

        return CdsWrapper.makeCardData(
                "NONAME",
                "123",
                "4012001011000771",
                CdsWrapper.makeExpDate(month, year)
        );
    }
}
