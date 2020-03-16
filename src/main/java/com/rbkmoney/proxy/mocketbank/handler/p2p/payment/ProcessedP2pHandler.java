package com.rbkmoney.proxy.mocketbank.handler.p2p.payment;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import com.rbkmoney.cds.client.storage.utils.BankCardExtractor;
import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.p2p_adapter.Context;
import com.rbkmoney.damsel.p2p_adapter.ProcessResult;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.java.damsel.utils.creators.P2pAdapterCreators;
import com.rbkmoney.java.damsel.utils.extractors.P2pAdapterExtractors;
import com.rbkmoney.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import com.rbkmoney.proxy.mocketbank.configuration.properties.TimerProperties;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rbkmoney.java.damsel.constant.Error.DEFAULT_ERROR_CODE;
import static com.rbkmoney.java.damsel.constant.Error.THREE_DS_NOT_FINISHED;
import static com.rbkmoney.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus.isAuthenticationAvailable;
import static com.rbkmoney.proxy.mocketbank.utils.UrlUtils.prepareRedirectParams;
import static com.rbkmoney.proxy.mocketbank.utils.model.CardAction.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedP2pHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    public ProcessResult process(Context context) throws TException {
        byte[] state = context.getSession().getState();
        if (state != null && state.length > 0) {
            return ErrorBuilder.prepareP2pError(errorMapping, DEFAULT_ERROR_CODE, THREE_DS_NOT_FINISHED);
        }

        BankCard bankCard = P2pAdapterExtractors.extractBankCardSender(context);
        CardData cardData = cds.getCardData(bankCard.getToken());
        CardDataProxyModel cardDataProxyModel = BankCardExtractor.initCardDataProxyModel(bankCard, cardData);

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            com.rbkmoney.damsel.p2p_adapter.Intent intent = P2pAdapterCreators.createFinishIntentSuccess();
            TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
            CardAction action = CardAction.findByValue(card.get().getAction());
            if (CardAction.isCardEnrolled(card.get())) {
                return prepareEnrolledP2pResult(context, intent, transactionInfo, cardDataProxyModel);
            }
            return prepareNotEnrolledP2pResult(intent, transactionInfo, action);
        }
        return ErrorBuilder.prepareP2pError(errorMapping, UNSUPPORTED_CARD);
    }

    private ProcessResult prepareNotEnrolledP2pResult(com.rbkmoney.damsel.p2p_adapter.Intent intent, TransactionInfo transactionInfo, CardAction action) {
        if (isCardSuccess(action)) {
            return P2pAdapterCreators.createP2pResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }
        CardAction currentAction = isCardFailed(action) ? action : UNKNOWN_FAILURE;
        return ErrorBuilder.prepareP2pError(errorMapping, currentAction);
    }

    private ProcessResult prepareEnrolledP2pResult(Context context, com.rbkmoney.damsel.p2p_adapter.Intent intent, TransactionInfo transactionInfo, CardDataProxyModel cardData) {
        com.rbkmoney.damsel.p2p_adapter.Intent currentIntent = intent;
        VerifyEnrollmentResponse verifyEnrollmentResponse = mpiApi.verifyEnrollment(cardData);
        if (isAuthenticationAvailable(verifyEnrollmentResponse.getEnrolled())) {
            String tag = SuspendPrefix.P2P.getPrefix() + P2pAdapterCreators.createTransactionId(context);
            String termUrl = UrlUtils.getCallbackUrl(mockBankProperties.getCallbackUrl(), mockBankProperties.getPathP2pCallbackUrl());
            currentIntent = prepareRedirect(context, verifyEnrollmentResponse, tag, termUrl);
        }
        byte[] state = StateUtils.prepareState(verifyEnrollmentResponse);
        return P2pAdapterCreators.createP2pResult(currentIntent, state, transactionInfo);
    }

    private com.rbkmoney.damsel.p2p_adapter.Intent prepareRedirect(Context context, VerifyEnrollmentResponse verifyEnrollmentResponse, String tag, String termUrl) {
        String url = verifyEnrollmentResponse.getAcsUrl();
        Map<String, String> params = prepareRedirectParams(verifyEnrollmentResponse, tag, termUrl);
        Map<String, String> options = context.getOptions();
        int timerRedirectTimeout = extractRedirectTimeout(options, timerProperties.getRedirectTimeout());
        String userInteractionID = P2pAdapterExtractors.extractSessionId(context);

        return P2pAdapterCreators.createIntentWithSleepIntent(
                timerRedirectTimeout,
                P2pAdapterCreators.createPostUserInteraction(userInteractionID, url, params),
                tag
        );
    }
}
