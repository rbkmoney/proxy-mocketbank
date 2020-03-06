package com.rbkmoney.proxy.mocketbank.handler.payment.callback;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.proxy_provider.PaymentCallbackProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentCallbackResult;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.damsel.constant.Error;
import com.rbkmoney.java.damsel.constant.PaymentState;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.utils.Converter;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import com.rbkmoney.proxy.mocketbank.utils.model.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.proxy.mocketbank.utils.creator.ProxyProviderCreator.createDefaultTransactionInfo;
import static com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus.isAuthenticationSuccessful;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCallbackHandler {

    private final CdsClientStorage cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;

    public PaymentCallbackResult handler(ByteBuffer byteBuffer, PaymentContext context) {
        HashMap<String, String> parameters = Converter.mergeParams(byteBuffer, context.getSession().getState());
        CardDataProxyModel cardData = cds.getCardData(context);
        ValidatePaResResponse validatePaResResponse = mpiApi.validatePaRes(cardData, parameters);
        if (isAuthenticationSuccessful(validatePaResResponse.getTransactionStatus())) {
            TransactionInfo transactionInfo = createDefaultTransactionInfo(context);
            PaymentCallbackProxyResult proxyResult = createCallbackProxyResult(
                    createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo
            );
            return createCallbackResult("".getBytes(), proxyResult);
        }

        CardAction action = CardUtils.extractActionFromCard(cardList, cardData);
        return ErrorBuilder.prepareCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, action);
    }

}
