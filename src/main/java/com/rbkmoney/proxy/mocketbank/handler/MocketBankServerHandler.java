package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy.Intent;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.utils.CardUtils;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.cds.CdsApi;
import com.rbkmoney.proxy.mocketbank.utils.damsel.*;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MocketBankMpiApi;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MocketBankMpiUtils;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiEnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiTransactionStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction.*;

@Component
public class MocketBankServerHandler implements ProviderProxySrv.Iface {

    private static final Logger LOGGER = LoggerFactory.getLogger(MocketBankServerHandler.class);

    @Autowired
    private CdsApi cds;

    @Autowired
    private MocketBankMpiApi mocketBankMpiApi;

    @Value("${proxy-mocketbank.callbackUrl}")
    private String callbackUrl;

    @Value("${fixture.cards}")
    private Resource fixtureCards;

    @Value("${timer.timeout}")
    private int timerTimeout;

    private List<Card> cardList;

    @PostConstruct
    public void init() throws IOException {
        cardList = CardUtils.getCardListFromFile(fixtureCards.getInputStream());
    }

    /**
     * Запрос к прокси на создание многоразового токена
     */
    @Override
    public RecurrentTokenGenerationProxyResult generateToken(RecurrentTokenGenerationContext context) throws TException {
        LOGGER.info("GenerateToken start");

        String session = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentSessionId();
        String token = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();

        String recurrentToken = token;
        com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();

        CardData cardData;
        try {
            LOGGER.info("GenerateToken: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException e) {
            LOGGER.error("GenerateToken: CDS Exception", e);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResultFailure("GenerateToken: CDS Exception", e.getMessage());
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());

        if (card.isPresent()) {
            MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());

            if (!cardUtils.isEnrolled(card)) {
                String error;
                switch (action) {
                    case INSUFFICIENT_FUNDS:
                        error = INSUFFICIENT_FUNDS.getAction();
                        break;
                    case INVALID_CARD:
                        error = INVALID_CARD.getAction();
                        break;
                    case CVV_MATCH_FAIL:
                        error = CVV_MATCH_FAIL.getAction();
                        break;
                    case EXPIRED_CARD:
                        error = EXPIRED_CARD.getAction();
                        break;
                    case SUCCESS:
                        RecurrentTokenGenerationProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResult(
                                intent,
                                "processed".getBytes(),
                                recurrentToken
                        );
                        LOGGER.info("GenerateToken: success {}", proxyResult);
                        return proxyResult;
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                RecurrentTokenGenerationProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResultFailure(error, error);
                LOGGER.info("GenerateToken: failure {}", proxyResult);
                return proxyResult;
            }

        } else {
            RecurrentTokenGenerationProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResultFailure(
                    UNSUPPORTED_CARD.getAction(),
                    UNSUPPORTED_CARD.getAction()
            );
            LOGGER.info("GenerateToken: failure {}", proxyResult);
            return proxyResult;
        }

        VerifyEnrollmentResponse verifyEnrollmentResponse = null;
        try {
            verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                    cardData.getPan(),
                    cardData.getExpDate().getYear(),
                    cardData.getExpDate().getMonth()
            );
        } catch (IOException e) {
            LOGGER.error("GenerateToken: Exception in verifyEnrollment", e);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResultFailure(
                    UNKNOWN_FAILURE.getAction(),
                    UNKNOWN_FAILURE.getAction()
            );
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = "MPI-" + context.getTokenInfo().getPaymentTool().getId();
            LOGGER.info("GenerateToken: suspend tag {}", tag);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MocketBankMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            LOGGER.info("GenerateToken: prepare redirect params {}", params);

            intent = ProxyWrapper.makeIntentWithSuspendIntent(
                    tag, BaseWrapper.makeTimerTimeout(timerTimeout),
                    UserInteractionWrapper.makeUserInteraction(
                            UserInteractionWrapper.makeBrowserPostRequest(
                                    url,
                                    params
                            )
                    )
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(MocketBankMpiUtils.PA_REQ, verifyEnrollmentResponse.getPaReq());

        LOGGER.info("GenerateToken: Extra map {}", extra);

        byte[] state;
        try {
            state = Converter.mapToByteArray(extra);
        } catch (IOException e) {
            LOGGER.error("GenerateToken: Converter Exception", e);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResultFailure("Converter", e.getMessage());
        }

        RecurrentTokenGenerationProxyResult result = ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResult(
                intent, state, recurrentToken
        );

        LOGGER.info("GenerateToken: finish {}", result);
        return result;
    }

    /**
     * Запрос к прокси на обработку обратного вызова от провайдера в рамках сессии получения
     * многоразового токена.
     */
    @Override
    public RecurrentTokenGenerationCallbackResult handleRecurrentTokenGenerationCallback(ByteBuffer byteBuffer, RecurrentTokenGenerationContext context) throws TException {
        LOGGER.info("RecurrentTokenGenerationCallbackResult: start");

        String session = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentSessionId();
        String token = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();

        Map<String, String> options = context.getOptions();
        String recurrentToken = token;
        HashMap<String, String> parameters;

        LOGGER.info("RecurrentTokenGenerationCallbackResult: merge input parameters");
        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
        } catch (Exception e) {
            LOGGER.error("RecurrentTokenGenerationCallbackResult: Parse ByteBuffer Exception", e);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationCallbackResultFailure(
                    "error".getBytes(),
                    "RecurrentTokenGenerationCallbackResult: Parse ByteBuffer Exception",
                    e.getMessage()
            );
        }
        LOGGER.info("RecurrentTokenGenerationCallbackResult: merge input parameters {}", parameters);

        CardData cardData;
        try {
            LOGGER.info("RecurrentTokenGenerationCallbackResult: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException e) {
            LOGGER.error("RecurrentTokenGenerationCallbackResult: CDS Exception", e);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationCallbackResultFailure(
                    "error".getBytes(),
                    "RecurrentTokenGenerationCallbackResult: CDS Exception",
                    e.getMessage()
            );
        }

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (IOException e) {
            LOGGER.error("RecurrentTokenGenerationCallbackResult: Exception", e);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationCallbackResultFailure(
                    "error".getBytes(),
                    "RecurrentTokenGenerationCallbackResult: Exception",
                    e.getMessage()
            );
        }
        LOGGER.info("RecurrentTokenGenerationCallbackResult: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();

            RecurrentTokenGenerationProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenGenerationProxyResult(
                    intent,
                    "processed".getBytes(),
                    recurrentToken
            );

            LOGGER.info("RecurrentTokenGenerationCallbackResult: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return ProxyProviderWrapper.makeRecurrentTokenGenerationCallbackResult(callbackResponse, proxyResult);
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());
        MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());
        String error;

        switch (action) {
            case THREE_D_SECURE_FAILURE:
                error = THREE_D_SECURE_FAILURE.getAction();
                break;
            case THREE_D_SECURE_TIMEOUT:
                error = THREE_D_SECURE_TIMEOUT.getAction();
                break;
            default:
                error = UNKNOWN_FAILURE.getAction();

        }

        RecurrentTokenGenerationCallbackResult callbackResult = ProxyProviderWrapper.makeRecurrentTokenGenerationCallbackResultFailure(
                "error".getBytes(), "RecurrentTokenGenerationCallbackResult: error", error
        );

        LOGGER.info("RecurrentTokenGenerationCallbackResult: callbackResult {}", callbackResult);
        return callbackResult;
    }

    // Тут токен
    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {

        Map<String, String> options = (context.getOptions().size() > 0) ? context.getOptions() : new HashMap<>();

        TargetInvoicePaymentStatus target = context.getSession().getTarget();

        if (target.isSetProcessed()) {
            return processed(context, options);
        } else if (target.isSetCaptured()) {
            return captured(context, options);
        } else if (target.isSetCancelled()) {
            return cancelled(context, options);
        } else if (target.isSetRefunded()) {
            return refunded(context, options);
        } else {
            LOGGER.error("Error unsupported method");
            return ProxyProviderWrapper.makeProxyResultFailure("Unsupported method", "Unsupported method");
        }

    }

    private PaymentProxyResult processed(PaymentContext context, Map<String, String> options) {
        LOGGER.info("Processed start");
        com.rbkmoney.damsel.proxy_provider.InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();

        CardData cardData;
        try {

            if(invoicePayment.getPaymentResource().isSetRecurrentPaymentResource()) {
                cardData = cds.getCardData(invoicePayment.getPaymentResource().getRecurrentPaymentResource().getRecToken());
            } else {
                String session = invoicePayment.getPaymentResource().getDisposablePaymentResource().getPaymentSessionId();
                String token = invoicePayment.getPaymentResource().getDisposablePaymentResource().getPaymentTool().getBankCard().getToken();
                LOGGER.info("Processed: call CDS. Token {}, session: {}", token, session);
                cardData = cds.getSessionCardData(token, session);
            }

        } catch (TException e) {
            LOGGER.error("Processed: CDS Exception", e);
            return ProxyProviderWrapper.makeProxyResultFailure("Processed: CDS Exception", e.getMessage());
        }

        TransactionInfo transactionInfo = null;
        com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();


        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());

        if (card.isPresent()) {
            MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());

            if (!cardUtils.isEnrolled(card)) {
                String error;
                switch (action) {
                    case INSUFFICIENT_FUNDS:
                        error = INSUFFICIENT_FUNDS.getAction();
                        break;
                    case INVALID_CARD:
                        error = INVALID_CARD.getAction();
                        break;
                    case CVV_MATCH_FAIL:
                        error = CVV_MATCH_FAIL.getAction();
                        break;
                    case EXPIRED_CARD:
                        error = EXPIRED_CARD.getAction();
                        break;
                    case SUCCESS:
                        transactionInfo = DomainWrapper.makeTransactionInfo(
                                MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                                Collections.emptyMap()
                        );
                        PaymentProxyResult proxyResult = ProxyProviderWrapper.makePaymentProxyResult(
                                intent,
                                "captured".getBytes(),
                                transactionInfo
                        );
                        LOGGER.info("Processed: success {}", proxyResult);
                        return proxyResult;
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                PaymentProxyResult proxyResult = ProxyProviderWrapper.makeProxyResultFailure(error, error);
                LOGGER.info("Processed: failure {}", proxyResult);
                return ProxyProviderWrapper.makeProxyResultFailure(error, error);
            }

        } else {
            PaymentProxyResult proxyResult = ProxyProviderWrapper.makeProxyResultFailure(
                    UNSUPPORTED_CARD.getAction(),
                    UNSUPPORTED_CARD.getAction()
            );
            LOGGER.info("Processed: failure {}", proxyResult);
            return proxyResult;
        }

        VerifyEnrollmentResponse verifyEnrollmentResponse = null;
        try {
            verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                    cardData.getPan(),
                    cardData.getExpDate().getYear(),
                    cardData.getExpDate().getMonth()
            );
        } catch (IOException e) {
            LOGGER.error("Processed: Exception in verifyEnrollment", e);
            return ProxyProviderWrapper.makeProxyResultFailure(
                    UNKNOWN_FAILURE.getAction(),
                    UNKNOWN_FAILURE.getAction()
            );
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = "MPI-" + MocketBankMpiUtils.generateInvoice(context.getPaymentInfo());
            LOGGER.info("Processed: suspend tag {}", tag);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MocketBankMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            LOGGER.info("Processed: prepare redirect params {}", params);

            intent = ProxyWrapper.makeIntentWithSuspendIntent(
                    tag, BaseWrapper.makeTimerTimeout(timerTimeout),
                    UserInteractionWrapper.makeUserInteraction(
                            UserInteractionWrapper.makeBrowserPostRequest(
                                    url,
                                    params
                            )
                    )
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(MocketBankMpiUtils.PA_REQ, verifyEnrollmentResponse.getPaReq());

        LOGGER.info("Processed: Extra map {}", extra);

        byte[] state;
        try {
            state = Converter.mapToByteArray(extra);
        } catch (IOException e) {

            LOGGER.error("Processed: Converter Exception", e);
            return ProxyProviderWrapper.makeProxyResultFailure("Converter", e.getMessage());
        }

        PaymentProxyResult proxyResult = ProxyProviderWrapper.makePaymentProxyResult(intent, state, transactionInfo);
        LOGGER.info("Processed: finish {}", proxyResult);
        return proxyResult;
    }

    private PaymentProxyResult captured(PaymentContext context, Map<String, String> options) {
        LOGGER.info("Captured: start");
        com.rbkmoney.damsel.proxy_provider.InvoicePayment payment = context.getPaymentInfo().getPayment();
        TransactionInfo transactionInfoContractor = payment.getTrx();
        TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
                transactionInfoContractor.getId(),
                transactionInfoContractor.getExtra()
        );

        context.getSession().setState("confirm".getBytes());

        Intent intent = ProxyWrapper.makeFinishIntentSuccess();
        PaymentProxyResult proxyResult = ProxyProviderWrapper.makePaymentProxyResult(intent, "confirm".getBytes(), transactionInfo);

        LOGGER.info("Captured: proxyResult {}", proxyResult);
        return proxyResult;
    }

    private PaymentProxyResult cancelled(PaymentContext context, Map<String, String> options) {
        PaymentProxyResult proxyResult = ProxyProviderWrapper.makePaymentProxyResult(
                ProxyWrapper.makeFinishIntentSuccess(),
                "cancelled".getBytes(),
                context.getPaymentInfo().getPayment().getTrx()
        );
        LOGGER.info("Cancelled: proxyResult {}", proxyResult);
        return proxyResult;
    }

    private PaymentProxyResult refunded(PaymentContext context, Map<String, String> options) {
        PaymentProxyResult proxyResult = ProxyProviderWrapper.makePaymentProxyResult(
                ProxyWrapper.makeFinishIntentSuccess(),
                "refunded".getBytes(),
                context.getPaymentInfo().getPayment().getTrx()
        );
        LOGGER.info("Refunded: proxyResult {}", proxyResult);
        return proxyResult;
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer, PaymentContext context) throws TException {
        LOGGER.info("HandlePaymentCallback: start");
        InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        String session = invoicePayment.getPaymentResource().getDisposablePaymentResource().getPaymentSessionId();
        String token = invoicePayment.getPaymentResource().getDisposablePaymentResource().getPaymentTool().getBankCard().getToken();
        Map<String, String> options = context.getOptions();

        HashMap<String, String> parameters;

        LOGGER.info("HandlePaymentCallback: merge input parameters");
        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
        } catch (Exception e) {
            LOGGER.error("HandlePaymentCallback: Parse ByteBuffer Exception", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "HandlePaymentCallback: Parse ByteBuffer Exception",
                    e.getMessage()
            );
        }
        LOGGER.info("HandlePaymentCallback: merge input parameters {}", parameters);

        CardData cardData;
        try {
            LOGGER.info("HandlePaymentCallback: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException e) {
            LOGGER.error("HandlePaymentCallback: CDS Exception", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "HandlePaymentCallback: CDS Exception",
                    e.getMessage()
            );
        }

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (IOException e) {
            LOGGER.error("HandlePaymentCallback: Exception", e);
            return ProxyProviderWrapper.makeCallbackResultFailure(
                    "error".getBytes(),
                    "HandlePaymentCallback: Exception",
                    e.getMessage()
            );
        }
        LOGGER.info("HandlePaymentCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy.Intent intent = ProxyWrapper.makeFinishIntentSuccess();

            TransactionInfo transactionInfo = DomainWrapper.makeTransactionInfo(
                    MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                    Collections.emptyMap()
            );

            PaymentCallbackProxyResult proxyResult = ProxyProviderWrapper.makeCallbackProxyResult(
                    intent, "captured".getBytes(), transactionInfo
            );

            LOGGER.info("HandlePaymentCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return ProxyProviderWrapper.makeCallbackResult(callbackResponse, proxyResult);
        }

        CardUtils cardUtils = new CardUtils(cardList);
        Optional<Card> card = cardUtils.getCardByPan(cardData.getPan());
        MocketBankMpiAction action = MocketBankMpiAction.findByValue(card.get().getAction());
        String error;

        switch (action) {
            case THREE_D_SECURE_FAILURE:
                error = THREE_D_SECURE_FAILURE.getAction();
                break;
            case THREE_D_SECURE_TIMEOUT:
                error = THREE_D_SECURE_TIMEOUT.getAction();
                break;
            default:
                error = UNKNOWN_FAILURE.getAction();

        }

        PaymentCallbackResult callbackResult = ProxyProviderWrapper.makeCallbackResultFailure(
                "error".getBytes(), "HandlePaymentCallback: error", error
        );

        LOGGER.info("HandlePaymentCallback: callbackResult {}", callbackResult);
        return callbackResult;
    }

}
