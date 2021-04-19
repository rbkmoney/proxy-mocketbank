package com.rbkmoney.proxy.mocketbank.handler.p2p.callback;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.exception.CdsStorageExpDateException;
import com.rbkmoney.cds.client.storage.utils.BankCardExtractor;
import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.p2p_adapter.Callback;
import com.rbkmoney.damsel.p2p_adapter.CallbackResult;
import com.rbkmoney.damsel.p2p_adapter.Context;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.java.damsel.utils.creators.P2pAdapterCreators;
import com.rbkmoney.java.damsel.utils.extractors.P2pAdapterExtractors;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
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

import static com.rbkmoney.java.damsel.constant.Error.DEFAULT_ERROR_CODE;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus.isAuthenticationSuccessful;

@Slf4j
@Component
@RequiredArgsConstructor
public class P2pCallbackHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;

    public CallbackResult handleCallback(Callback callback, Context context) {
        HashMap<String, String> parameters =
                Converter.mergeParams(ByteBuffer.wrap(callback.getPayload()), context.getSession().getState());

        BankCard bankCard = P2pAdapterExtractors.extractBankCardSender(context);
        CardData cardData = cds.getCardData(bankCard.getToken());

        CardDataProxyModel cardDataProxyModel;
        try {
            cardDataProxyModel =  BankCardExtractor.initCardDataProxyModel(bankCard, cardData);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareP2pCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        ValidatePaResResponse validatePaResResponse = mpiApi.validatePaRes(cardDataProxyModel, parameters);
        if (isAuthenticationSuccessful(validatePaResResponse.getTransactionStatus())) {
            TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
            return P2pAdapterCreators.createP2pCallbackResult(
                    P2pAdapterCreators.createFinishIntentSuccess(),
                    PaymentState.CAPTURED.getBytes(),
                    transactionInfo
            );
        }

        CardAction action = CardUtils.extractActionFromCard(cardList, cardDataProxyModel);
        return ErrorBuilder.prepareP2pCallbackResultFailure(errorMapping, DEFAULT_ERROR_CODE, action);
    }

}
