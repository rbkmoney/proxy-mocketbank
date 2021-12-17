package com.rbkmoney.proxy.mocketbank.handler.payment.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.exception.CdsStorageExpDateException;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.SessionState;
import com.rbkmoney.proxy.mocketbank.service.mpi20.processor.Mpi20Processor;
import com.rbkmoney.proxy.mocketbank.utils.*;
import com.rbkmoney.proxy.mocketbank.utils.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.*;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus.isAuthenticationSuccessful;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCallbackHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final ObjectMapper objectMapper;
    private final Mpi20Processor mpi20Processor;
    private final CallbackResponseWithTemplateCreator callbackResponseCreator;

    public PaymentCallbackResult handler(ByteBuffer byteBuffer, PaymentContext context) {
        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Card card = CardUtils.extractCardByPan(cardList, cardData.getPan())
                .orElseThrow(() -> new IllegalStateException("Card must be set"));

        if (CardAction.isCardEnrolled(card)) {
            return processEnrolled(byteBuffer, context, cardData);
        } else if (CardAction.isCardEnrolled20(card)) {
            CardAction cardAction = CardAction.findByValue(card.getAction());
            return processEnrolled20(context, CardUtils.getHttpMethodByCardAction(cardAction));
        } else {
            throw new RuntimeException("Card should be enrolled");
        }
    }

    @SneakyThrows
    private PaymentCallbackResult processEnrolled20(PaymentContext context, String formMethod) {
        SessionState contextSessionState = objectMapper.readValue(context.getSession().getState(), SessionState.class);
        switch (contextSessionState.getState()) {
            case PREPARE:
                PaymentCallbackProxyResult authCallbackProxyResult = mpi20Processor.processAuth(context);
                SessionState authSessionState = objectMapper.readValue(
                        authCallbackProxyResult.getNextState(),
                        SessionState.class);
                return createCallbackResult(
                        callbackResponseCreator.createCallbackResponseWithForm(
                                authSessionState.getOptions(),
                                formMethod,
                                context
                        ),
                        authCallbackProxyResult);
            case AUTH:
                return createCallbackResult(new byte[]{}, mpi20Processor.processResult(context));
            default:
                throw new IllegalStateException("Illegal state");
        }
    }


    private PaymentCallbackResult processEnrolled(
            ByteBuffer byteBuffer,
            PaymentContext context,
            CardDataProxyModel cardData) {
        HashMap<String, String> parameters = Converter.mergeParams(byteBuffer, context.getSession().getState());
        ValidatePaResResponse validatePaResResponse = mpiApi.validatePaRes(cardData, parameters);
        if (isAuthenticationSuccessful(validatePaResResponse.getTransactionStatus())) {
            TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
            PaymentCallbackProxyResult proxyResult = createCallbackProxyResult(
                    createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo
            );
            return createCallbackResult("".getBytes(), proxyResult);
        }

        CardAction action = CardUtils.extractActionFromCard(cardList, cardData);
        return ErrorBuilder.prepareCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, action);
    }

}
