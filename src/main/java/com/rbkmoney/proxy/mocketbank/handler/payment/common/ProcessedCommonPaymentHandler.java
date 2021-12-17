package com.rbkmoney.proxy.mocketbank.handler.payment.common;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.exception.CdsStorageExpDateException;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.timeout_behaviour.TimeoutBehaviour;
import com.rbkmoney.damsel.user_interaction.UserInteraction;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.configuration.properties.TimerProperties;
import com.rbkmoney.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.service.mpi20.processor.Mpi20Processor;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
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
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rbkmoney.java.damsel.constant.Error.DEFAULT_ERROR_CODE;
import static com.rbkmoney.java.damsel.constant.Error.THREE_DS_NOT_FINISHED;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractInvoiceId;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isMakeRecurrent;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus.isAuthenticationAvailable;
import static com.rbkmoney.proxy.mocketbank.utils.UrlUtils.prepareRedirectParams;
import static com.rbkmoney.proxy.mocketbank.utils.TokenProviderVerification.hasBankCardTokenProvider;
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
    private final Mpi20Processor mpi20Processor;

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
        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
        if (hasBankCardTokenProvider(context)) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }

        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            CardAction action = CardAction.findByValue(card.get().getAction());
            if (CardAction.isCardEnrolled(card.get())) {
                return prepareEnrolledPaymentProxyResult(context, intent, transactionInfo, cardData, action);
            } else if (CardAction.isCardEnrolled20(card.get())) {
                return mpi20Processor.processPrepare(context);
            }
            return prepareNotEnrolledPaymentProxyResult(intent, transactionInfo, action);
        }
        return ErrorBuilder.prepareError(errorMapping, UNSUPPORTED_CARD);
    }

    private PaymentProxyResult prepareNotEnrolledPaymentProxyResult(
            Intent intent,
            TransactionInfo transactionInfo,
            CardAction action) {
        if (isCardSuccess(action)) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }
        CardAction currentAction = isCardFailed(action) ? action : UNKNOWN_FAILURE;
        return ErrorBuilder.prepareError(errorMapping, currentAction);
    }

    private PaymentProxyResult prepareEnrolledPaymentProxyResult(
            PaymentContext context,
            Intent intent,
            TransactionInfo transactionInfo,
            CardDataProxyModel cardData,
            CardAction action) {
        Intent currentIntent = intent;
        VerifyEnrollmentResponse verifyEnrollmentResponse = mpiApi.verifyEnrollment(cardData);
        if (isAuthenticationAvailable(verifyEnrollmentResponse.getEnrolled())) {
            String tag = SuspendPrefix.PAYMENT.getPrefix() +
                    ProxyProviderPackageCreators.createInvoiceWithPayment(context.getPaymentInfo());
            MultiValueMap<String, String> terminationUrl = UrlUtils.getTerminationUrlAsParam(
                    context.getPaymentInfo().getPayment(),
                    mockBankProperties.getFinishInteraction()
            );
            String termUrl = UrlUtils.getCallbackUrl(
                    mockBankProperties.getCallbackUrl(),
                    mockBankProperties.getPathCallbackUrl(),
                    terminationUrl
            );
            currentIntent = prepareRedirect(context, verifyEnrollmentResponse, tag, termUrl, action);
        }
        byte[] state = StateUtils.prepareState(verifyEnrollmentResponse);
        return createPaymentProxyResult(currentIntent, state, transactionInfo);
    }

    private Intent prepareRedirect(
            PaymentContext context,
            VerifyEnrollmentResponse verifyEnrollmentResponse,
            String tag,
            String termUrl,
            CardAction action) {
        String url = verifyEnrollmentResponse.getAcsUrl();
        Map<String, String> params = prepareRedirectParams(verifyEnrollmentResponse, tag, termUrl);
        Map<String, String> options = context.getOptions();
        int timerRedirectTimeout = extractRedirectTimeout(options, timerProperties.getRedirectTimeout());

        Intent intent = createIntentWithSuspendIntent(
                tag, timerRedirectTimeout, prepareUserInteraction(url, params, action)
        );
        Failure failure = errorMapping.mapFailure(DEFAULT_ERROR_CODE, THREE_DS_NOT_FINISHED);
        intent.getSuspend().setTimeoutBehaviour(TimeoutBehaviour.operation_failure(OperationFailure.failure(failure)));
        return intent;
    }

    private UserInteraction prepareUserInteraction(String url,
                                                   Map<String, String> params,
                                                   CardAction action) {
        if (CardAction.isGetAcsCard(action)) {
            return createGetUserInteraction(UrlUtils.prepareUrlWithParams(url, params));
        }
        return createPostUserInteraction(url, params);
    }

}
