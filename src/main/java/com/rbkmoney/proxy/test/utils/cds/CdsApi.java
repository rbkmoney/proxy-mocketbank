package com.rbkmoney.proxy.test.utils.cds;

import com.rbkmoney.damsel.cds.*;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.List;

@Component
public class CdsApi {

    private final static Logger LOGGER = LoggerFactory.getLogger(CdsApi.class);

    @Autowired
    private KeyringSrv.Iface keyringSrv;

    @Autowired
    private StorageSrv.Iface storageSrv;

    /**
     * Разблокировать ключи
     *
     * @param key_share ByteBuffer
     * @return UnlockStatus
     * @throws TException
     */
    public UnlockStatus unlock(ByteBuffer key_share) throws TException {
        LOGGER.info("Keyring: unlock");
        return keyringSrv.unlock(key_share);
    }

    /**
     * Инициализация ключей
     *
     * @param threshold  short
     * @param num_shares short
     * @return List<ByteBuffer>
     * @throws TException
     */
    public List<ByteBuffer> init(short threshold, short num_shares) throws TException {
        LOGGER.info("Keyring: init");
        return keyringSrv.init(threshold, num_shares);
    }

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
        LOGGER.info("Storage getCardData: response {}", cardData.toString());
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
        LOGGER.info("Storage getSessionCardData: response {}", cardData.toString());
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
        LOGGER.info("Storage putCardData: cardData {} ", cardData);
        PutCardDataResult result = storageSrv.putCardData(cardData);
        LOGGER.info("Storage putCardData: response {}", result.toString());
        return result;
    }

}
