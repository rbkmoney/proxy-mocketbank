package com.rbkmoney.proxy.mocketbank.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.InvoicePaymentRefund;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.utils.CardUtils;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MocketBankMpiApi;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MocketBankMpiUtils;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.*;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isUndefinedResultOrUnavailable;
import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MocketBankMpiAction.*;

@Slf4j
@Component
public class MocketBankServerHandler implements ProviderProxySrv.Iface {

    @Autowired
    private CdsClientStorage cds;

    @Autowired
    private MocketBankMpiApi mocketBankMpiApi;

    @Autowired
    private ErrorMapping errorMapping;

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

    @Override
    public RecurrentTokenProxyResult generateToken(RecurrentTokenContext context) throws TException {
        String recurrentId = context.getTokenInfo().getPaymentTool().getId();
        log.info("GenerateToken: start with recurrentId {}", recurrentId);

        String token = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();

        RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(token);

        RecurrentTokenProxyResult proxyResult;
        // Applepay, Samsungpay, Googlepay - always successful and does not depends on card
        Optional<BankCardTokenProvider> bankCardTokenProvider = getBankCardTokenProvider(context);
        if (bankCardTokenProvider.isPresent()) {
            proxyResult = createRecurrentTokenProxyResult(intent);
            log.info("Processed: success {} with invoiceId {}", proxyResult, recurrentId);
            return proxyResult;
        }

        CardData cardData = cds.getCardData(token);

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
                        proxyResult = createRecurrentTokenProxyResult(
                                intent,
                                PaymentState.PROCESSED.getBytes()
                        );
                        log.info("GenerateToken: success {} with recurrentId {}", proxyResult, recurrentId);
                        return proxyResult;
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                proxyResult = createRecurrentTokenProxyResultFailure(
                        errorMapping.getFailureByCodeAndDescription(error, error)
                );
                log.info("GenerateToken: failure {} with recurrentId {}", proxyResult, recurrentId);
                return proxyResult;
            }

        } else {
            proxyResult = createRecurrentTokenProxyResultFailure(
                    errorMapping.getFailureByCodeAndDescription(
                            UNSUPPORTED_CARD.getAction(),
                            UNSUPPORTED_CARD.getAction()
                    )
            );
            log.info("GenerateToken: failure {} with recurrentId {}", proxyResult, recurrentId);
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
            String message = "GenerateToken: Exception in verifyEnrollment with recurrentId " + recurrentId;
            log.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = MocketBankTag.RECURRENT_SUSPEND_TAG + context.getTokenInfo().getPaymentTool().getId();
            log.info("GenerateToken: suspend tag {} with recurrentId {}", tag, recurrentId);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MocketBankMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            log.info("GenerateToken: prepare redirect params {} with recurrentId {}", params, recurrentId);

            intent = createRecurrentTokenWithSuspendIntent(
                    tag, timerTimeout, createPostUserInteraction(url, params)
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(MocketBankMpiUtils.PA_REQ, verifyEnrollmentResponse.getPaReq());

        log.info("GenerateToken: Extra map {} with recurrentId {}", extra, recurrentId);

        byte[] state;
        try {
            state = Converter.mapToByteArray(extra);
        } catch (JsonProcessingException ex) {
            String message = "GenerateToken: can't convert map to byte array with recurrentId " + recurrentId;
            log.error(message);
            throw new IllegalArgumentException(message, ex);
        }

        RecurrentTokenProxyResult result = createRecurrentTokenProxyResult(
                intent, state
        );

        log.info("GenerateToken: finish {} with recurrentId {} ", result, recurrentId);
        return result;
    }

    @Override
    public RecurrentTokenCallbackResult handleRecurrentTokenCallback(ByteBuffer byteBuffer, RecurrentTokenContext context) throws TException {
        String recurrentId = context.getTokenInfo().getPaymentTool().getId();
        log.info("handleRecurrentTokenCallback start with invoiceId {}", recurrentId);

        HashMap<String, String> parameters;
        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
            log.info("handleRecurrentTokenCallback: merge params: recurrentId {}, params {}", recurrentId, parameters);
        } catch (Exception ex) {
            String message = "handleRecurrentTokenCallback:  merge params error with recurrentId " + recurrentId;
            log.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        String token = context.getTokenInfo().getPaymentTool().getPaymentResource().getPaymentTool().getBankCard().getToken();
        CardData cardData = cds.getCardData(token);

        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (IOException ex) {
            String message = "handleRecurrentTokenCallback: Exception";
            log.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }
        log.info("handleRecurrentTokenCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(token);

            RecurrentTokenProxyResult proxyResult = createRecurrentTokenProxyResult(
                    intent,
                    "processed".getBytes()
            );

            log.info("handleRecurrentTokenCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return createRecurrentTokenCallbackResult(callbackResponse, proxyResult);
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

        RecurrentTokenCallbackResult callbackResult = createRecurrentTokenCallbackResultFailure(
                "error".getBytes(), errorMapping.getFailureByCodeAndDescription("error", error)
        );

        log.info("handleRecurrentTokenCallback finish {}, recurrent {}", callbackResult, recurrentId);
        return callbackResult;
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("processPayment start with invoiceId {}", invoiceId);
        Map<String, String> options = (context.getOptions().size() > 0) ? context.getOptions() : new HashMap<>();

        TargetInvoicePaymentStatus target = context.getSession().getTarget();
        try {
            if (target.isSetProcessed()) {
                return processed(context, options);
            } else if (target.isSetCaptured()) {
                return captured(context, options);
            } else if (target.isSetCancelled()) {
                return cancelled(context, options);
            } else if (target.isSetRefunded()) {
                return refunded(context, options);
            } else {
                PaymentProxyResult proxyResult = createProxyResultFailure(
                        errorMapping.getFailureByCodeAndDescription(
                                "Unsupported method",
                                "Unsupported method"
                        )
                );
                log.error("Error unsupported method. proxyResult {} with invoiceId {}", proxyResult, invoiceId);
                return proxyResult;
            }

        } catch (Exception ex) {
            String message = "Exception in processPayment with invoiceId " + invoiceId;
            if (isUndefinedResultOrUnavailable(ex)) {
                log.warn(message, ex);
            } else {
                log.error(message, ex);
            }
            throw ex;
        }
    }

    private PaymentProxyResult processed(PaymentContext context, Map<String, String> options) {
        com.rbkmoney.damsel.proxy_provider.InvoicePayment invoicePayment = context.getPaymentInfo().getPayment();
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("Processed start with invoiceId {}", invoiceId);
        CardData cardData;
        if (invoicePayment.getPaymentResource().isSetRecurrentPaymentResource()) {
            cardData = cds.getCardData(invoicePayment.getPaymentResource().getRecurrentPaymentResource().getPaymentTool().getBankCard().getToken());
        } else {
            cardData = cds.getCardData(context);
        }
        log.info("CardData: {}, pan: {}", cardData, cardData.getPan());

        TransactionInfo transactionInfo = null;
        Intent intent = createFinishIntentSuccess();
        if (context.getPaymentInfo().getPayment().isSetMakeRecurrent()
                && context.getPaymentInfo().getPayment().isMakeRecurrent()) {
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        PaymentProxyResult proxyResult;
        // Applepay, Samsungpay, Googlepay - always successful and does not depends on card
        Optional<BankCardTokenProvider> bankCardTokenProvider = getBankCardTokenProvider(context);
        if (bankCardTokenProvider.isPresent()) {
            transactionInfo = createTransactionInfo(
                    MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                    Collections.emptyMap()
            );
            proxyResult = createPaymentProxyResult(
                    intent,
                    PaymentState.CAPTURED.getBytes(),
                    transactionInfo
            );
            log.info("Processed: success {} with invoiceId {}", proxyResult, invoiceId);
            return proxyResult;
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
                    case APPLE_PAY_FAILURE:
                        error = APPLE_PAY_FAILURE.getAction();
                        break;

                    case SAMSUNG_PAY_FAILURE:
                        error = SAMSUNG_PAY_FAILURE.getAction();
                        break;

                    case GOOGLE_PAY_FAILURE:
                        error = GOOGLE_PAY_FAILURE.getAction();
                        break;

                    case EXPIRED_CARD:
                        error = EXPIRED_CARD.getAction();
                        break;

                    case SUCCESS:
                        transactionInfo = createTransactionInfo(
                                MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                                Collections.emptyMap()
                        );
                        proxyResult = createPaymentProxyResult(
                                intent,
                                PaymentState.CAPTURED.getBytes(),
                                transactionInfo
                        );
                        log.info("Processed: success {} with invoiceId {}", proxyResult, invoiceId);
                        return proxyResult;
                    default:
                        error = UNKNOWN_FAILURE.getAction();

                }
                proxyResult = createProxyResultFailure(
                        errorMapping.getFailureByCodeAndDescription(error, error)
                );
                log.info("Processed: failure {} with invoiceId {}", proxyResult, invoiceId);
                return proxyResult;
            }

        } else {
            proxyResult = createProxyResultFailure(
                    errorMapping.getFailureByCodeAndDescription(
                            UNSUPPORTED_CARD.getAction(),
                            UNSUPPORTED_CARD.getAction()
                    )
            );
            errorMapping.getFailureByCodeAndDescription(UNSUPPORTED_CARD.getAction(), UNSUPPORTED_CARD.getAction());
            log.info("Processed: failure {} with invoiceId {}", proxyResult, invoiceId);
            return proxyResult;
        }

        if (invoicePayment.getPaymentResource().isSetRecurrentPaymentResource()) {
            transactionInfo = createTransactionInfo(
                    MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                    Collections.emptyMap()
            );
            proxyResult = createPaymentProxyResult(
                    intent,
                    PaymentState.CAPTURED.getBytes(),
                    transactionInfo
            );
            log.info("Processed: success {} with invoiceId {}", proxyResult, invoiceId);
            return proxyResult;
        }

        VerifyEnrollmentResponse verifyEnrollmentResponse;
        try {
            verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                    cardData.getPan(),
                    cardData.getExpDate().getYear(),
                    cardData.getExpDate().getMonth()
            );
        } catch (IOException ex) {
            String message = "Processed: Exception in verifyEnrollment with invoiceId " + invoiceId;
            log.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        if (verifyEnrollmentResponse.getEnrolled().equals(MocketBankMpiEnrollmentStatus.AUTHENTICATION_AVAILABLE)) {
            String tag = MocketBankTag.PAYMENT_SUSPEND_TAG + MocketBankMpiUtils.generateInvoice(context.getPaymentInfo());
            log.info("Processed: suspend tag {} with invoiceId {}", tag, invoiceId);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MocketBankMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            log.info("Processed: prepare redirect params {} with invoiceId {}", params, invoiceId);

            intent = createIntentWithSuspendIntent(
                    tag, timerTimeout, createPostUserInteraction(url, params)
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(MocketBankMpiUtils.PA_REQ, verifyEnrollmentResponse.getPaReq());

        log.info("Processed: Extra map {} with invoiceId {}", extra, invoiceId);
        byte[] state;
        try {
            state = Converter.mapToByteArray(extra);
        } catch (JsonProcessingException ex) {
            String message = "Processed: can't convert map to byte array with invoiceId " + invoiceId;
            log.error(message);
            throw new IllegalArgumentException(message, ex);
        }

        proxyResult = createPaymentProxyResult(intent, state, transactionInfo);
        log.info("Processed: finish {} with invoiceId {}", proxyResult, invoiceId);
        return proxyResult;
    }

    private PaymentProxyResult captured(PaymentContext context, Map<String, String> options) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("Captured start with invoiceId {}", invoiceId);

        com.rbkmoney.damsel.proxy_provider.InvoicePayment payment = context.getPaymentInfo().getPayment();
        TransactionInfo transactionInfoContractor = payment.getTrx();
        TransactionInfo transactionInfo = createTransactionInfo(
                transactionInfoContractor.getId(),
                transactionInfoContractor.getExtra()
        );

        context.getSession().setState(PaymentState.CONFIRM.getBytes());

        Intent intent = createFinishIntentSuccess();
        if (context.getPaymentInfo().getPayment().isSetMakeRecurrent()
                && context.getPaymentInfo().getPayment().isMakeRecurrent()) {
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        PaymentProxyResult proxyResult = createPaymentProxyResult(intent, PaymentState.CONFIRM.getBytes(), transactionInfo);

        log.info("Captured: proxyResult {} with invoiceId {}", proxyResult, invoiceId);
        return proxyResult;
    }

    private PaymentProxyResult cancelled(PaymentContext context, Map<String, String> options) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("Cancelled start with invoiceId {}", invoiceId);
        PaymentProxyResult proxyResult = createPaymentProxyResult(
                createFinishIntentSuccess(),
                PaymentState.CANCELLED.getBytes(),
                context.getPaymentInfo().getPayment().getTrx()
        );
        log.info("Cancelled: proxyResult {} with invoiceId {}", proxyResult, invoiceId);
        return proxyResult;
    }

    private PaymentProxyResult refunded(PaymentContext context, Map<String, String> options) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("Refunded start with invoiceId {}", invoiceId);
        InvoicePaymentRefund invoicePaymentRefund = context.getPaymentInfo().getRefund();

        PaymentProxyResult proxyResult = createPaymentProxyResult(
                createFinishIntentSuccess(),
                PaymentState.REFUNDED.getBytes(),
                invoicePaymentRefund.getTrx()
        );
        log.info("Refunded end: proxyResult {} with invoiceId {}", proxyResult, invoiceId);
        return proxyResult;
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer byteBuffer, PaymentContext context) throws TException {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("handlePaymentCallback start with invoiceId {}", invoiceId);

        HashMap<String, String> parameters;

        try {
            parameters = (HashMap<String, String>) Converter.byteArrayToMap(context.getSession().getState());
            parameters.putAll(Converter.byteBufferToMap(byteBuffer));
            log.info("handlePaymentCallback: merge params: invoiceId {}, params {}", invoiceId, parameters);
        } catch (Exception ex) {
            String message = "handlePaymentCallback: merge params error with invoiceId " + invoiceId;
            log.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }

        CardData cardData = cds.getCardData(context);
        ValidatePaResResponse validatePaResResponse;
        try {
            validatePaResResponse = mocketBankMpiApi.validatePaRes(cardData.getPan(), parameters.get("paRes"));
        } catch (Exception ex) {
            String message = "handlePaymentCallback: Exception";
            log.error(message, ex);
            throw new IllegalArgumentException(message, ex);
        }
        log.info("handlePaymentCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MocketBankMpiTransactionStatus.AUTHENTICATION_SUCCESSFUL)) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy_provider.Intent intent = createFinishIntentSuccess();

            TransactionInfo transactionInfo = createTransactionInfo(
                    MocketBankMpiUtils.generateInvoice(context.getPaymentInfo()),
                    Collections.emptyMap()
            );

            PaymentCallbackProxyResult proxyResult = createCallbackProxyResult(
                    intent, PaymentState.CAPTURED.getBytes(), transactionInfo
            );

            log.info("handlePaymentCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return createCallbackResult(callbackResponse, proxyResult);
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

        PaymentCallbackResult callbackResult = createCallbackResultFailure(
                "error".getBytes(), errorMapping.getFailureByCodeAndDescription(
                        "HandlePaymentCallback: error", error
                )
        );

        log.info("handlePaymentCallback finish {}, invoice {}", callbackResult, invoiceId);
        return callbackResult;
    }

    public static Optional<BankCardTokenProvider> getBankCardTokenProvider(PaymentContext context) {

        Optional<PaymentResource> paymentResource = Optional.ofNullable(context.getPaymentInfo())
                .map(PaymentInfo::getPayment)
                .map(InvoicePayment::getPaymentResource);

        if (paymentResource.isPresent() && paymentResource.get().isSetDisposablePaymentResource()) {
            return Optional.ofNullable(context.getPaymentInfo())
                    .map(PaymentInfo::getPayment)
                    .map(InvoicePayment::getPaymentResource)
                    .map(PaymentResource::getDisposablePaymentResource)
                    .map(DisposablePaymentResource::getPaymentTool)
                    .map(PaymentTool::getBankCard)
                    .map(BankCard::getTokenProvider);
        }

        return Optional.empty();
    }

    public static Optional<BankCardTokenProvider> getBankCardTokenProvider(RecurrentTokenContext context) {

        Optional<DisposablePaymentResource> paymentResource = Optional.ofNullable(context.getTokenInfo())
                .map(RecurrentTokenInfo::getPaymentTool)
                .map(RecurrentPaymentTool::getPaymentResource);

        if (paymentResource.isPresent()) {
            return paymentResource
                    .map(DisposablePaymentResource::getPaymentTool)
                    .map(PaymentTool::getBankCard)
                    .map(BankCard::getTokenProvider);
        }

        return Optional.empty();
    }

}
