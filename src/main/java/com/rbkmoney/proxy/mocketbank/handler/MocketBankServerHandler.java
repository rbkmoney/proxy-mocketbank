package com.rbkmoney.proxy.mocketbank.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.damsel.proxy_provider.InvoicePaymentRefund;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.PaymentUtils;
import com.rbkmoney.proxy.mocketbank.utils.error_mapping.ErrorMapping;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MockMpiApi;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.MockMpiUtils;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiAction;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiEnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiField;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiTransactionStatus;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.ValidatePaResRequest;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.VerifyEnrollmentRequest;
import com.rbkmoney.proxy.mocketbank.utils.mocketbank.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.*;

import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractBankCard;
import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTransactionInfo;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isUndefinedResultOrUnavailable;
import static com.rbkmoney.proxy.mocketbank.utils.mocketbank.constant.MpiAction.*;

@Slf4j
@Component
public class MocketBankServerHandler implements ProviderProxySrv.Iface {

    @Autowired
    private CdsClientStorage cds;

    @Autowired
    private MockMpiApi mocketBankMpiApi;

    @Autowired
    private ErrorMapping errorMapping;

    @Value("${proxy-mocketbank.callbackUrl}")
    private String callbackUrl;

    @Value("${fixture.cards}")
    private Resource fixtureCards;

    @Value("${timer.timeout}")
    private int timerTimeout;

    @Autowired
    private List<Card> cardList;

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
        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());

        if (card.isPresent()) {
            MpiAction action = MpiAction.findByValue(card.get().getAction());
            if (!MpiAction.isCardEnrolled(card.get())) {
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
                                PaymentState.PROCESSED.getState().getBytes()
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

        BankCard bankCard = extractBankCard(context);
        VerifyEnrollmentResponse verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                VerifyEnrollmentRequest.builder()
                        .pan(cardData.getPan())
                        .year(cardData.getExpDate().getYear())
                        .month(cardData.getExpDate().getMonth())
                        .build());

        if (verifyEnrollmentResponse.getEnrolled().equals(MpiEnrollmentStatus.AUTHENTICATION_AVAILABLE.getStatus())) {
            String tag = SuspendPrefix.RECURRENT.getPrefix() + context.getTokenInfo().getPaymentTool().getId();
            log.info("GenerateToken: suspend tag {} with recurrentId {}", tag, recurrentId);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MockMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            log.info("GenerateToken: prepare redirect params {} with recurrentId {}", params, recurrentId);

            intent = createRecurrentTokenWithSuspendIntent(
                    tag, timerTimeout, createPostUserInteraction(url, params)
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(MpiField.PA_REQ.getValue(), verifyEnrollmentResponse.getPaReq());

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

        ValidatePaResResponse validatePaResResponse = mocketBankMpiApi.validatePaRes(
                    ValidatePaResRequest.builder()
                    .pan(cardData.getPan())
                    .paRes(parameters.get("paRes"))
                    .build());

        log.info("handleRecurrentTokenCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MpiTransactionStatus.AUTHENTICATION_SUCCESSFUL.getStatus())) {
            byte[] callbackResponse = new byte[0];
            RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(token);

            RecurrentTokenProxyResult proxyResult = createRecurrentTokenProxyResult(
                    intent,
                    "processed".getBytes()
            );

            log.info("handleRecurrentTokenCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return createRecurrentTokenCallbackResult(callbackResponse, proxyResult);
        }

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        MpiAction action = MpiAction.findByValue(card.get().getAction());
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
                    PaymentUtils.generateTransactionId(context.getPaymentInfo()),
                    Collections.emptyMap()
            );
            proxyResult = createPaymentProxyResult(
                    intent,
                    PaymentState.CAPTURED.getState().getBytes(),
                    transactionInfo
            );
            log.info("Processed: success {} with invoiceId {}", proxyResult, invoiceId);
            return proxyResult;
        }


        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            MpiAction action = MpiAction.findByValue(card.get().getAction());

            if (!MpiAction.isCardEnrolled(card.get())) {
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
                                PaymentUtils.generateTransactionId(context.getPaymentInfo()),
                                Collections.emptyMap()
                        );
                        proxyResult = createPaymentProxyResult(
                                intent,
                                PaymentState.CAPTURED.getState().getBytes(),
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
                    PaymentUtils.generateTransactionId(context.getPaymentInfo()),
                    Collections.emptyMap()
            );
            proxyResult = createPaymentProxyResult(
                    intent,
                    PaymentState.CAPTURED.getState().getBytes(),
                    transactionInfo
            );
            log.info("Processed: success {} with invoiceId {}", proxyResult, invoiceId);
            return proxyResult;
        }

        BankCard bankCard = extractBankCard(context);
        VerifyEnrollmentResponse verifyEnrollmentResponse = mocketBankMpiApi.verifyEnrollment(
                    VerifyEnrollmentRequest.builder()
                    .pan(cardData.getPan())
                    .year(bankCard.getExpDate().getYear())
                    .month(bankCard.getExpDate().getMonth())
                    .build());


        if (verifyEnrollmentResponse.getEnrolled().equals(MpiEnrollmentStatus.AUTHENTICATION_AVAILABLE.getStatus())) {
            String tag = SuspendPrefix.PAYMENT.getPrefix() + PaymentUtils.generateTransactionId(context.getPaymentInfo());
            log.info("Processed: suspend tag {} with invoiceId {}", tag, invoiceId);

            String url = verifyEnrollmentResponse.getAcsUrl();
            Map<String, String> params = new HashMap<>();
            params.put("PaReq", verifyEnrollmentResponse.getPaReq());
            params.put("MD", tag);
            params.put("TermUrl", MockMpiUtils.getCallbackUrl(callbackUrl, "/mocketbank/term_url{?termination_uri}"));

            log.info("Processed: prepare redirect params {} with invoiceId {}", params, invoiceId);

            intent = createIntentWithSuspendIntent(
                    tag, timerTimeout, createPostUserInteraction(url, params)
            );
        }

        Map<String, String> extra = new HashMap<>();
        extra.put(MpiField.PA_REQ.getValue(), verifyEnrollmentResponse.getPaReq());

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

        context.getSession().setState(PaymentState.CONFIRM.getState().getBytes());

        Intent intent = createFinishIntentSuccess();
        if (context.getPaymentInfo().getPayment().isSetMakeRecurrent()
                && context.getPaymentInfo().getPayment().isMakeRecurrent()) {
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        PaymentProxyResult proxyResult = createPaymentProxyResult(intent, PaymentState.CONFIRM.getState().getBytes(), transactionInfo);

        log.info("Captured: proxyResult {} with invoiceId {}", proxyResult, invoiceId);
        return proxyResult;
    }

    private PaymentProxyResult cancelled(PaymentContext context, Map<String, String> options) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("Cancelled start with invoiceId {}", invoiceId);
        PaymentProxyResult proxyResult = createPaymentProxyResult(
                createFinishIntentSuccess(),
                PaymentState.CANCELLED.getState().getBytes(),
                context.getPaymentInfo().getPayment().getTrx()
        );
        log.info("Cancelled: proxyResult {} with invoiceId {}", proxyResult, invoiceId);
        return proxyResult;
    }

    private PaymentProxyResult refunded(PaymentContext context, Map<String, String> options) {
        String invoiceId = context.getPaymentInfo().getInvoice().getId();
        log.info("Refunded start with invoiceId {}", invoiceId);
        InvoicePaymentRefund invoicePaymentRefund = context.getPaymentInfo().getRefund();
        TransactionInfo paymentRefundTrx = invoicePaymentRefund.getTrx();
        TransactionInfo transactionInfo;
        if (paymentRefundTrx == null || paymentRefundTrx.getId() == null) {
            transactionInfo = createTransactionInfo(invoicePaymentRefund.getId(), options);
        } else {
            transactionInfo = paymentRefundTrx;
        }

        PaymentProxyResult proxyResult = createPaymentProxyResult(
                createFinishIntentSuccess(),
                PaymentState.REFUNDED.getState().getBytes(),
                transactionInfo
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
        ValidatePaResResponse validatePaResResponse = mocketBankMpiApi.validatePaRes(
                ValidatePaResRequest.builder()
                        .pan(cardData.getPan())
                        .paRes(parameters.get("paRes"))
                        .build());
        log.info("handlePaymentCallback: validatePaResResponse {}", validatePaResResponse);

        if (validatePaResResponse.getTransactionStatus().equals(MpiTransactionStatus.AUTHENTICATION_SUCCESSFUL.getStatus())) {
            byte[] callbackResponse = new byte[0];
            com.rbkmoney.damsel.proxy_provider.Intent intent = createFinishIntentSuccess();

            TransactionInfo transactionInfo = createTransactionInfo(
                    PaymentUtils.generateTransactionId(context.getPaymentInfo()),
                    Collections.emptyMap()
            );

            PaymentCallbackProxyResult proxyResult = createCallbackProxyResult(
                    intent, PaymentState.CAPTURED.getState().getBytes(), transactionInfo
            );

            log.info("handlePaymentCallback: callbackResponse {}, proxyResult {}", callbackResponse, proxyResult);
            return createCallbackResult(callbackResponse, proxyResult);
        }

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        MpiAction action = MpiAction.findByValue(card.get().getAction());
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
