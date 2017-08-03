package com.rbkmoney.proxy.mocketbank.utils.cds;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.cds.StorageSrv;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CdsApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(CdsApi.class);

    @Autowired
    private StorageSrv.Iface storageSrv;

    /**
     * Получить карточные данные без CVV
     *
     * @param token String
     * @return CardData
     * @throws TException
     */
    public CardData getCardData(String token) throws TException {
        LOGGER.info("Storage getCardData: token {}", token);
        CardData cardData = storageSrv.getCardData(token);
        LOGGER.info("Storage getCardData: response token {}", token);
        return cardData;
    }

    /**
     * Получить карточные данные с CVV
     *
     * @param token   String
     * @param session String
     * @return CardData
     * @throws TException
     */
    public CardData getSessionCardData(String token, String session) throws TException {
        LOGGER.info("Storage getSessionCardData: token {}, session{} ", token, session);
        CardData cardData = storageSrv.getSessionCardData(token, session);
        LOGGER.info("Storage getSessionCardData: response token {}, session{}", token, session);
        return cardData;
    }

    /**
     * Положить карточные данные
     *
     * @param cardData CardData
     * @return PutCardDataResult
     * @throws TException
     */
    public PutCardDataResult putCardData(CardData cardData) throws TException {
        LOGGER.info("Storage putCardData: cardData");
        PutCardDataResult result = storageSrv.putCardData(cardData);
        LOGGER.info("Storage putCardData: response");
        return result;
    }

}
