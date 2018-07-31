package com.rbkmoney.proxy.mocketbank.utils.cds;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.PutCardDataResult;
import com.rbkmoney.damsel.cds.SessionData;
import com.rbkmoney.damsel.cds.StorageSrv;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.withdrawals.domain.Destination;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CdsStorageApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsStorageApi.class);

    private StorageSrv.Iface storageSrv;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link CdsStorageApi CdsApi} instance.
     */
    public CdsStorageApi() {
        // Constructs default a new {@link CdsApi CdsApi} instance.
    }

    /**
     * Constructs a new {@link CdsStorageApi CdsApi} instance with the given
     * initial parameters to be constructed.
     *
     * @param storageSrv the field's storageSrv (see {@link #storageSrv}).
     */
    @Autowired
    public CdsStorageApi(StorageSrv.Iface storageSrv) {
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
        LOGGER.info("getCardData: token: {}", token);
        try {
            CardData cardData = storageSrv.getCardData(token);
            LOGGER.info("getCardData: response, token: {}");
            return cardData;
        } catch (Exception ex) {
            throw new CdsException(String.format("Exception in getCardData with token: %s", token), ex);
        }
    }

    public CardData getCardData(final PaymentContext context) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();

        InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        DisposablePaymentResource disposablePaymentResource = invoicePayment.getPaymentResource().getDisposablePaymentResource();

        if (!disposablePaymentResource.getPaymentTool().getBankCard().isSetToken()) {
            throw new CdsException("getSessionCardData: Token must be set, invoiceId " + invoiceId);
        }

        String token = disposablePaymentResource.getPaymentTool().getBankCard().getToken();
        return getCardData(token);
    }

    public SessionData getSessionData(final PaymentContext context) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();

        InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        DisposablePaymentResource disposablePaymentResource = invoicePayment.getPaymentResource().getDisposablePaymentResource();

        if (!disposablePaymentResource.isSetPaymentSessionId()) {
            throw new CdsException("getSessionData: Session must be set, invoiceId " + invoiceId);
        }

        String session = disposablePaymentResource.getPaymentSessionId();
        try {
            SessionData sessionData = storageSrv.getSessionData(session);
            LOGGER.info("Storage getSessionData: finish");
            return sessionData;
        } catch (Exception ex) {
            throw new CdsException("Exception in getSessionData with SessionData", ex);
        }
    }

    public CardData getCardData(final Withdrawal withdrawal) {
        String withdrawalId = withdrawal.getId();

        Optional<String> token = Optional.ofNullable(withdrawal.getDestination())
                .map(Destination::getBankCard)
                .map(BankCard::getToken);

        if (!token.isPresent()) {
            throw new CdsException("getCardData: Token must be set, withdrawalId " + withdrawalId);
        }

        return getCardData(token.get());
    }


    /**
     * Put the card data
     *
     * @param cardData CardData
     * @return PutCardDataResult
     * @throws CdsException
     */
    public PutCardDataResult putCardData(CardData cardData, SessionData sessionData) throws CdsException {
        LOGGER.info("Storage putCardData - start");
        try {
            PutCardDataResult result = storageSrv.putCardData(cardData, sessionData);
            LOGGER.info("Storage putCardData: finish");
            return result;
        } catch (Exception ex) {
            throw new CdsException("Exception in putCardData with cardData", ex);
        }
    }

}

