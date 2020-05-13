package com.rbkmoney.proxy.mocketbank.handler.payment.recurrent;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.exception.CdsStorageExpDateException;
import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.domain.OperationFailure;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenIntent;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenProxyResult;
import com.rbkmoney.damsel.timeout_behaviour.TimeoutBehaviour;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.configuration.properties.TimerProperties;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import com.rbkmoney.proxy.mocketbank.utils.model.CardUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.StateUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rbkmoney.java.damsel.constant.Error.DEFAULT_ERROR_CODE;
import static com.rbkmoney.java.damsel.constant.Error.THREE_DS_NOT_FINISHED;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractRecurrentId;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.hasBankCardTokenProvider;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus.isAuthenticationAvailable;
import static com.rbkmoney.proxy.mocketbank.utils.UrlUtils.prepareRedirectParams;
import static com.rbkmoney.proxy.mocketbank.utils.model.CardAction.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateTokenHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    public RecurrentTokenProxyResult handler(RecurrentTokenContext context) {
        String recurrentId = extractRecurrentId(context);
        RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(recurrentId);

        // Applepay, Samsungpay, Googlepay - always successful and does not depends on card
        if (hasBankCardTokenProvider(context)) {
            return createRecurrentTokenProxyResult(intent);
        }

        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareRecurrentTokenError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            CardAction action = CardAction.findByValue(card.get().getAction());
            if (CardAction.isCardEnrolled(card.get())) {
                return prepareEnrolledRecurrentTokenProxyResult(context, intent, cardData);
            }
            return prepareNotEnrolledRecurrentTokenProxyResult(intent, action);
        }
        return ErrorBuilder.prepareRecurrentTokenError(errorMapping, UNSUPPORTED_CARD);
    }

    private RecurrentTokenProxyResult prepareNotEnrolledRecurrentTokenProxyResult(RecurrentTokenIntent intent, CardAction action) {
        if (isCardSuccess(action)) {
            return createRecurrentTokenProxyResult(intent, PaymentState.CAPTURED.getBytes());
        }
        CardAction currentAction = isCardFailed(action) ? action : UNKNOWN_FAILURE;
        return ErrorBuilder.prepareRecurrentTokenError(errorMapping, currentAction);
    }

    private RecurrentTokenProxyResult prepareEnrolledRecurrentTokenProxyResult(RecurrentTokenContext context, RecurrentTokenIntent intent, CardDataProxyModel cardData) {
        RecurrentTokenIntent recurrentTokenIntent = intent;
        VerifyEnrollmentResponse verifyEnrollmentResponse = mpiApi.verifyEnrollment(cardData);
        if (isAuthenticationAvailable(verifyEnrollmentResponse.getEnrolled())) {
            String tag = SuspendPrefix.RECURRENT.getPrefix() + context.getTokenInfo().getPaymentTool().getId();
            String termUrl = UrlUtils.getCallbackUrl(mockBankProperties.getCallbackUrl(), mockBankProperties.getPathRecurrentCallbackUrl());
            recurrentTokenIntent = prepareRedirect(context, verifyEnrollmentResponse, tag, termUrl);
        }
        byte[] state = StateUtils.prepareState(verifyEnrollmentResponse);
        return createRecurrentTokenProxyResult(recurrentTokenIntent, state);
    }

    private RecurrentTokenIntent prepareRedirect(RecurrentTokenContext context, VerifyEnrollmentResponse verifyEnrollmentResponse, String tag, String termUrl) {
        String url = verifyEnrollmentResponse.getAcsUrl();
        Map<String, String> params = prepareRedirectParams(verifyEnrollmentResponse, tag, termUrl);
        Map<String, String> options = context.getOptions();
        int timerRedirectTimeout = extractRedirectTimeout(options, timerProperties.getRedirectTimeout());

        RecurrentTokenIntent recurrentTokenIntent = createRecurrentTokenWithSuspendIntent(
                tag, timerRedirectTimeout, createPostUserInteraction(url, params)
        );
        Failure failure = errorMapping.mapFailure(DEFAULT_ERROR_CODE, THREE_DS_NOT_FINISHED);
        recurrentTokenIntent.getSuspend().setTimeoutBehaviour(TimeoutBehaviour.operation_failure(OperationFailure.failure(failure)));
        return recurrentTokenIntent;
    }
}
