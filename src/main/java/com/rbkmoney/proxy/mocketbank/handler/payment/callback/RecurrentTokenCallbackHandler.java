package com.rbkmoney.proxy.mocketbank.handler.payment.callback;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.exception.CdsStorageExpDateException;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenCallbackResult;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenIntent;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenProxyResult;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import com.rbkmoney.proxy.mocketbank.utils.model.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors.extractRecurrentId;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus.isAuthenticationSuccessful;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecurrentTokenCallbackHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;

    public RecurrentTokenCallbackResult handler(ByteBuffer byteBuffer, RecurrentTokenContext context) {
        String recurrentId = extractRecurrentId(context);
        HashMap<String, String> parameters = Converter.mergeParams(byteBuffer, context.getSession().getState());

        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareRecurrentCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        ValidatePaResResponse validatePaResResponse = mpiApi.validatePaRes(cardData, parameters);
        if (isAuthenticationSuccessful(validatePaResResponse.getTransactionStatus())) {
            RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(recurrentId);
            RecurrentTokenProxyResult proxyResult =
                    createRecurrentTokenProxyResult(intent, PaymentState.PENDING.getBytes());
            return createRecurrentTokenCallbackResult("".getBytes(), proxyResult);
        }

        CardAction action = CardUtils.extractActionFromCard(cardList, cardData);
        return ErrorBuilder.prepareRecurrentCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, action);
    }
}
