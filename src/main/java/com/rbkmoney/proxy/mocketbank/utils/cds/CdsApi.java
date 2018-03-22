package com.rbkmoney.proxy.mocketbank.utils.cds;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.cds.StorageSrv;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CdsApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final StorageSrv.Iface storageSrv;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link CdsApi CdsApi} instance with the given
     * initial parameters to be constructed.
     *
     * @param storageSrv the field's storageSrv (see {@link #storageSrv}).
     */
    @Autowired
    public CdsApi(StorageSrv.Iface storageSrv) {
        this.storageSrv = storageSrv;
    }


    // ------------------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------------------

    /**
     * Get the card data without CVV
     *
     * @param token String
     * @return CardData
     * @throws CdsException
     */
    public CardData getCardData(final String token) {
        log.info("getCardData: token: {}", token);
        try {
            CardData cardData = storageSrv.getCardData(token);
            log.info("getCardData: response, token: {}");
            return cardData;
        } catch (Exception ex) {
            throw new CdsException(String.format("Exception in getCardData with token: %s", token), ex);
        }
    }

    /**
     * Get the card data with CVV
     *
     * @param token   String
     * @param session String
     * @return CardData
     * @throws CdsException
     */
    public CardData getSessionCardData(final String token, final String session) {
        log.info("getSessionCardData: token: {}, session: {} ", token, session);
        try {
            CardData cardData = storageSrv.getSessionCardData(token, session);
            log.info("getSessionCardData: response, token: {}, session: {}", token, session);
            return cardData;
        } catch (Exception ex) {
            throw new CdsException(String.format("Exception in getSessionCardData with token: %s, session: %s", token, session), ex);
        }
    }

    /**
     * Get the card data with CVV
     *
     * @param context PaymentContext
     * @return CardData
     * @throws CdsException
     */
    public CardData getSessionCardData(final PaymentContext context) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();

        InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        DisposablePaymentResource disposablePaymentResource = invoicePayment.getPaymentResource().getDisposablePaymentResource();

        if (!disposablePaymentResource.isSetPaymentSessionId()) {
            throw new CdsException("getSessionCardData: Session must be set, invoiceId " + invoiceId);
        }

        if (!disposablePaymentResource.getPaymentTool().getBankCard().isSetToken()) {
            throw new CdsException("getSessionCardData: Token must be set, invoiceId " + invoiceId);
        }

        String session = disposablePaymentResource.getPaymentSessionId();
        String token = disposablePaymentResource.getPaymentTool().getBankCard().getToken();
        return getSessionCardData(token, session);
    }

    public CardData getSessionCardData(final RecurrentTokenContext context) {
        String recurrentId = context.getTokenInfo().getPaymentTool().getId();

        DisposablePaymentResource disposablePaymentResource = context.getTokenInfo().getPaymentTool().getPaymentResource();

        if (!disposablePaymentResource.isSetPaymentSessionId()) {
            throw new CdsException("getSessionCardData: Session must be set, recurrentId " + recurrentId);
        }

        if (!disposablePaymentResource.getPaymentTool().getBankCard().isSetToken()) {
            throw new CdsException("getSessionCardData: Token must be set, recurrentId " + recurrentId);
        }

        String session = disposablePaymentResource.getPaymentSessionId();
        String token = disposablePaymentResource.getPaymentTool().getBankCard().getToken();
        return getSessionCardData(token, session);
    }

    /**
     * Put the card data
     *
     * @param cardData CardData
     * @return PutCardDataResult
     * @throws CdsException
     */
    public PutCardDataResult putCardData(CardData cardData) throws CdsException {
        log.info("Storage putCardData - start");
        try {
            PutCardDataResult result = storageSrv.putCardData(cardData);
            log.info("Storage putCardData: finish");
            return result;
        } catch (Exception ex) {
            throw new CdsException("Exception in putCardData with cardData", ex);
        }
    }

}
