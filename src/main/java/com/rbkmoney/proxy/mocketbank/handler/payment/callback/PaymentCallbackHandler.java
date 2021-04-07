package com.rbkmoney.proxy.mocketbank.handler.payment.callback;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.exception.CdsStorageExpDateException;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentCallbackProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentCallbackResult;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.Callback;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.SessionState;
import com.rbkmoney.proxy.mocketbank.service.mpi20.processor.Mpi20Processor;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import com.rbkmoney.proxy.mocketbank.utils.model.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public PaymentCallbackResult handler(ByteBuffer byteBuffer, PaymentContext context) {
        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Optional<Card> optionalCard = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (optionalCard.isEmpty()) {
            throw new IllegalStateException("Card must be set");
        }

        Card card = optionalCard.get();
        if (CardAction.isCardEnrolled(card)) {
            return processEnrolled(byteBuffer, context, cardData);
        } else if (CardAction.isCardEnrolled20(card)) {
            return processEnrolled20(context);
        } else {
            throw new RuntimeException("Card should be enrolled");
        }
    }

    @SneakyThrows
    private PaymentCallbackResult processEnrolled20(PaymentContext context) {
        SessionState sessionState = objectMapper.readValue(context.getSession().getState(), SessionState.class);
        switch (sessionState.getState()) {
            case PREPARE:
                return createCallbackResult(new byte[]{}, mpi20Processor.processAuth(context));
            case AUTH:
                return createCallbackResult(new byte[]{}, mpi20Processor.processResult(context));
            default:
                throw new IllegalStateException("Illegal state");
        }
    }



    private PaymentCallbackResult processEnrolled(ByteBuffer byteBuffer, PaymentContext context, CardDataProxyModel cardData) {
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
