package com.rbkmoney.proxy.mocketbank.handler.payment.common;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import com.rbkmoney.damsel.domain.TargetInvoicePaymentStatus;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.Intent;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.configuration.properties.TimerProperties;
import com.rbkmoney.proxy.mocketbank.utils.creator.ProxyProviderCreator;
import com.rbkmoney.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.ErrorHandler;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import com.rbkmoney.proxy.mocketbank.utils.model.CardUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.StateUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractInvoiceId;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isMakeRecurrent;
import static com.rbkmoney.proxy.mocketbank.utils.creator.ProxyProviderCreator.createDefaultTransactionInfo;
import static com.rbkmoney.proxy.mocketbank.utils.extractor.proxy.ProxyProviderPackageExtractors.hasBankCardTokenProvider;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus.isAuthenticationAvailable;
import static com.rbkmoney.proxy.mocketbank.utils.UrlUtils.prepareRedirectParams;
import static com.rbkmoney.proxy.mocketbank.utils.model.CardAction.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedCommonPaymentHandler implements CommonPaymentHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed() && paymentResource.isSetDisposablePaymentResource();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        Intent intent = createFinishIntentSuccess();
        if (isMakeRecurrent(context)) {
            String invoiceId = extractInvoiceId(context);
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        // Applepay, Samsungpay, Googlepay - always successful and does not depends on card
        TransactionInfo transactionInfo = createDefaultTransactionInfo(context);
        if (hasBankCardTokenProvider(context)) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }

        CardDataProxyModel cardData = cds.getCardData(context);
        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            CardAction action = CardAction.findByValue(card.get().getAction());
            if (CardAction.isCardEnrolled(card.get())) {
                return prepareEnrolledPaymentProxyResult(context, intent, transactionInfo, cardData);
            }
            return prepareNotEnrolledPaymentProxyResult(intent, transactionInfo, action);
        }
        return ErrorHandler.prepareError(errorMapping, UNSUPPORTED_CARD);
    }

    private PaymentProxyResult prepareNotEnrolledPaymentProxyResult(Intent intent, TransactionInfo transactionInfo, CardAction action) {
        if (isCardSuccess(action)) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }
        CardAction currentAction = isCardFailed(action) ? action : UNKNOWN_FAILURE;
        return ErrorHandler.prepareError(errorMapping, currentAction);
    }

    private PaymentProxyResult prepareEnrolledPaymentProxyResult(PaymentContext context, Intent intent, TransactionInfo transactionInfo, CardDataProxyModel cardData) {
        Intent currentIntent = intent;
        VerifyEnrollmentResponse verifyEnrollmentResponse = mpiApi.verifyEnrollment(cardData);
        if (isAuthenticationAvailable(verifyEnrollmentResponse.getEnrolled())) {
            String tag = SuspendPrefix.PAYMENT.getPrefix() + ProxyProviderCreator.createTransactionId(context.getPaymentInfo());
            String termUrl = UrlUtils.getCallbackUrl(mockBankProperties.getCallbackUrl(), mockBankProperties.getPathCallbackUrl());
            currentIntent = prepareRedirect(context, verifyEnrollmentResponse, tag, termUrl);
        }
        byte[] state = StateUtils.prepareState(verifyEnrollmentResponse);
        return createPaymentProxyResult(currentIntent, state, transactionInfo);
    }

    private Intent prepareRedirect(PaymentContext context, VerifyEnrollmentResponse verifyEnrollmentResponse, String tag, String termUrl) {
        String url = verifyEnrollmentResponse.getAcsUrl();
        Map<String, String> params = prepareRedirectParams(verifyEnrollmentResponse, tag, termUrl);
        Map<String, String> options = context.getOptions();
        int timerRedirectTimeout = extractRedirectTimeout(options, timerProperties.getRedirectTimeout());
        return createIntentWithSuspendIntent(
                tag, timerRedirectTimeout, createPostUserInteraction(url, params)
        );
    }
}
