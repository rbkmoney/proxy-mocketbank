//package com.rbkmoney.proxy.mocketbank.controller;
//
//import com.rbkmoney.damsel.cds.CardData;
//import com.rbkmoney.damsel.domain.TransactionInfo;
//import com.rbkmoney.damsel.proxy_provider.*;
//import com.rbkmoney.proxy.mocketbank.handler.common.Handler;
//import com.rbkmoney.proxy.mocketbank.handler.common.UnsupportedHandler;
//import com.rbkmoney.proxy.mocketbank.utils.CardUtils;
//import com.rbkmoney.proxy.mocketbank.utils.Converter;
//import com.rbkmoney.proxy.mocketbank.utils.cds.CdsApi;
//import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;
//import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyProviderWrapper;
//import com.rbkmoney.proxy.mocketbank.utils.damsel.ProxyWrapper;
//import com.rbkmoney.proxy.mocketbank.utils.model.Card;
//import com.rbkmoney.proxy.mocketbank.utils.mpi.model.ValidatePaResResponse;
//import org.apache.thrift.TException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Optional;
//

//holmes:
//        links:
//        - cds
//        image: dr.rbkmoney.com/rbkmoney/holmes:e1e866ad74f517c3370f167b6c7a6b7c948c53c6
//        command: [ "./scripts/cds/init-keyring.sh",
//        "./scripts/cds/unlock-keyring.sh" ]
//
//        cds:
//        image: dr.rbkmoney.com/rbkmoney/cds:2f84fde813bd1f21c1817f7f277763292dabf534
//        container_name: cds
//        ports:
//        - "8021:8022"
//        command: /opt/cds/bin/cds foreground
//        restart: on-failure:3

//import static com.rbkmoney.proxy.mocketbank.utils.mpi.constant.MpiAction.*;
//
//@Component
//public class MocketBankServerHandlerExample implements ProviderProxySrv.Iface {
//
//    private final static Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandlerExample.class);
//
//    @Autowired
//    private List<Handler> handlers;
//
//    @Autowired
//    private CdsApi cds;
//
//    @Override
//    public ProxyResult processPayment(Context context) throws TException {
//        return handlers.stream()
//                .filter(handler -> handler.filter(context.getSession().getTarget()))
//                .findFirst()
//                .orElse(new UnsupportedHandler())
//                .accept(context);
//    }
//
//    @Override
//    public CallbackResult handlePaymentCallback(ByteBuffer byteBuffer, Context context) throws TException {
//        LOGGER.info("HandlePaymentCallback: start");
//        InvoicePayment invoicePayment = context.getPayment().getPayment();
//        String token = invoicePayment.getPayer().getPaymentTool().getBankCard().getToken();
//        String session = invoicePayment.getPayer().getSession();
//
//
//        HashMap<String, String> parameters;
//
//        LOGGER.info("HandlePaymentCallback: merge input parameters");
//        try {
//            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
//            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
//        } catch (Exception e) {
//            LOGGER.error("HandlePaymentCallback: Parse ByteBuffer Exception", e);
//            return ProxyProviderWrapper.makeCallbackResultFailure(
//                    "error".getBytes(),
//                    "HandlePaymentCallback: Parse ByteBuffer Exception",
//                    e.getMessage()
//            );
//        }
//        LOGGER.info("HandlePaymentCallback: merge input parameters {}", parameters);
//
//        CardData cardData;
//        try {
//            LOGGER.info("HandlePaymentCallback: call CDS. Token {}, session: {}", token, session);
//            cardData = cds.getSessionCardData(token, session);
//        } catch (TException e) {
//            LOGGER.error("HandlePaymentCallback: CDS Exception", e);
//            return ProxyProviderWrapper.makeCallbackResultFailure(
//                    "error".getBytes(),
//                    "HandlePaymentCallback: CDS Exception",
//                    e.getMessage()
//            );
//        }
//
//        ValidatePaResResponse validatePaResResponse;
//        try {
//            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
//        } catch (IOException e) {
//            LOGGER.error("HandlePaymentCallback: Exception", e);
//            return ProxyProviderWrapper.makeCallbackResultFailure(
//                    "error".getBytes(),
//                    "HandlePaymentCallback: Exception",
//                    e.getMessage()
//            );
//        }
//        LOGGER.info("HandlePaymentCallback: validatePaResResponse {}", validatePaResResponse);
//
//        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
//            byte[] callbackResponse = new byte[0];
//            com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();
//
//            TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
//                    MocketBankMpiUtils.generateInvoice(context.getPayment()),
//                    Collections.emptyMap()
//            );
//
//            ProxyResult proxyResult = ProxyProviderWrapper.makeProxyResult(
//                    intent, "captured".getBytes(), transactionInfo
//            );
//
//            LOGGER.info("HandlePaymentCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
//            return ProxyProviderWrapper.makeCallbackResult(callbackResponse, proxyResult);
//        }
//
//        CardUtils cardUtils = new CardUtils(cardList);
//        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());
//        MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());
//        String error;
//
//        switch (action) {
//            case THREE_D_SECURE_FAILURE:
//                error = THREE_D_SECURE_FAILURE.getAction();
//                break;
//            case THREE_D_SECURE_TIMEOUT:
//                error = THREE_D_SECURE_TIMEOUT.getAction();
//                break;
//            default:
//                error = UNKNOWN_FAILURE.getAction();
//
//        }
//
//        CallbackResult callbackResult = ProxyProviderWrapper.makeCallbackResultFailure(
//                "error".getBytes(), "HandlePaymentCallback: error", error
//        );
//
//        LOGGER.info("HandlePaymentCallback: callbackResult {}", callbackResult);
//        return callbackResult;
//    }
//
//}

/**
 *
 *     @Override
public CallbackResult handlePaymentCallback(ByteBuffer byteBuffer, Context context) throws TException {
LOGGER.info("HandlePaymentCallback: start");
CallbackResult callbackResult = null;

try {
callbackResult = new PaymentCallback(context, byteBuffer)
.mergeParams()
.getCardDataFromCds()
.validatePaResInMpi()
.getCallbackResult();
} catch (CdsApi.CdsApiException exc) {
LOGGER.error(exc.getMessage());
callbackResult = ProxyProviderWrapper.makeCallbackResultFailure(
"error".getBytes(),
exc.getMessage(),
exc.getMessage()
);
}
return callbackResult;
}
 */
