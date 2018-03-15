package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
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
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankTag;
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

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        LOGGER.info("GenerateToken start");

        String session = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentSessionId();
        String token = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();

        RecurrentTokenIntent intent = ProxyProviderWrapper.makeRecurrentTokenFinishIntentSuccess(token);

        CardData cardData;
        try {
            LOGGER.info("GenerateToken: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (Exception ex) {
            String message = "GenerateToken: CDS Exceptiont";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
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
                        RecurrentTokenProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenProxyResult(
                                intent,
                                "processed".getBytes()
                        );
                        LOGGER.info("GenerateToken: success {}", proxyResult);
                        return proxyResult;
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                RecurrentTokenProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenProxyResultFailure(error, error);
                LOGGER.info("GenerateToken: failure {}", proxyResult);
                return proxyResult;
            }

        } else {
            RecurrentTokenProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenProxyResultFailure(
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
        } catch (IOException ex) {
            String message = "GenerateToken: Exception in verifyEnrollment";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = MocketBankTag.RECURRENT_SUSPEND_TAG + context.getTokenInfo().getPaymentTool().getId();
            LOGGER.info("GenerateToken: suspend tag {}", tag);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MocketBankMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            LOGGER.info("GenerateToken: prepare redirect params {}", params);

            intent = ProxyProviderWrapper.makeRecurrentTokenWithSuspendIntent(
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
        } catch (IOException ex) {
            String message = "GenerateToken: Converter Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        RecurrentTokenProxyResult result = ProxyProviderWrapper.makeRecurrentTokenProxyResult(
                intent, state
        );

        LOGGER.info("GenerateToken: finish {}", result);
        return result;
    }

    /**
     * Запрос к прокси на обработку обратного вызова от провайдера в рамках сессии получения
     * многоразового токена.
     */
    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer byteBuffer, RecurrentTokenContext context) throws TException {
        LOGGER.info("RecurrentTokenGenerationCallbackResult: start");

        String session = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentSessionId();
        String token = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();

        HashMap<String, String> parameters;

        LOGGER.info("RecurrentTokenGenerationCallbackResult: merge input parameters");
        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
        } catch (Exception ex) {
            String message = "RecurrentTokenGenerationCallbackResult: Parse ByteBuffer Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }
        LOGGER.info("RecurrentTokenGenerationCallbackResult: merge input parameters {}", parameters);

        CardData cardData;
        try {
            LOGGER.info("RecurrentTokenGenerationCallbackResult: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (TException ex) {
            String message = "RecurrentTokenGenerationCallbackResult: CDS Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (IOException ex) {
            String message = "RecurrentTokenGenerationCallbackResult: Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }
        LOGGER.info("RecurrentTokenGenerationCallbackResult: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            RecurrentTokenIntent intent = ProxyProviderWrapper.makeRecurrentTokenFinishIntentSuccess(token);

            RecurrentTokenProxyResult proxyResult = ProxyProviderWrapper.makeRecurrentTokenProxyResult(
                    intent,
                    "processed".getBytes()
            );

            LOGGER.info("RecurrentTokenGenerationCallbackResult: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return ProxyProviderWrapper.makeRecurrentTokenCallbackResult(callbackResponse, proxyResult);
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

        RecurrentTokenCallbackResult callbackResult = ProxyProviderWrapper.makeRecurrentTokenCallbackResultFailure(
                "error".getBytes(), "RecurrentTokenGenerationCallbackResult: error", error
        );

        LOGGER.info("RecurrentTokenGenerationCallbackResult: callbackResult {}", callbackResult);
        return callbackResult;
    }

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
        String invoiceId = invoicePayment.getId();
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
        } catch (Exception ex) {
            String message = "Processed: CDS Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        TransactionInfo transactionInfo = null;
        com.rbkmoney.damsel.proxy_provider.Intent intent = ProxyWrapper.makeFinishIntentSuccess();


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

        if(invoicePayment.getPaymentResource().isSetRecurrentPaymentResource()) {
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
        }

        VerifyEnrollmentResponse verifyEnrollmentResponse = null;
        try {
            verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                    cardData.getPan(),
                    cardData.getExpDate().getYear(),
                    cardData.getExpDate().getMonth()
            );
        } catch (IOException ex) {
            String message = "Processed: Exception in verifyEnrollment" + invoiceId;
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = MocketBankTag.PAYMENT_SUSPEND_TAG + MocketBankMpiUtils.generateInvoice(context.getPaymentInfo());
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
        } catch (IOException ex) {
            String message = "Processed: Converter Exception " + invoiceId;
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
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
        LOGGER.info("Refunded begin: context {}", context);
        InvoicePaymentRefund invoicePaymentRefund = context.getPaymentInfo().getRefund();

        PaymentProxyResult proxyResult = ProxyProviderWrapper.makePaymentProxyResult(
                ProxyWrapper.makeFinishIntentSuccess(),
                "refunded".getBytes(),
                invoicePaymentRefund.getTrx()
        );
        LOGGER.info("Refunded end: proxyResult {}", proxyResult);
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
        } catch (Exception ex) {
            String message = "HandlePaymentCallback: Parse ByteBuffer Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }
        LOGGER.info("HandlePaymentCallback: merge input parameters {}", parameters);

        CardData cardData;
        try {
            LOGGER.info("HandlePaymentCallback: call CDS. Token {}, session: {}", token, session);
            cardData = cds.getSessionCardData(token, session);
        } catch (Exception ex) {
            String message = "HandlePaymentCallback: CDS Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (Exception ex) {
            String message = "HandlePaymentCallback: Exception";
            LOGGER.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }
        LOGGER.info("HandlePaymentCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy_provider.Intent intent = ProxyWrapper.makeFinishIntentSuccess();

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
