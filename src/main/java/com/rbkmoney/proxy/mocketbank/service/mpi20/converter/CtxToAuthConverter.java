package com.rbkmoney.proxy.mocketbank.service.mpi20.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.damsel.proxy_provider.Cash;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.proxy.mocketbank.configuration.properties.Mpi20Properties;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.AuthenticationRequest;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.SessionState;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class CtxToAuthConverter implements Converter<PaymentContext, AuthenticationRequest> {

    private final CdsClientStorage cds;
    private final ObjectMapper objectMapper;
    private final Mpi20Properties mpi20Properties;

    @Override
    @SneakyThrows
    public AuthenticationRequest convert(PaymentContext context) {
        SessionState sessionState = objectMapper.readValue(context.getSession().getState(), SessionState.class);

        CardDataProxyModel cardData = cds.getCardData(context);

        Cash cost = context.getPaymentInfo().getPayment().getCost();
        return AuthenticationRequest.builder()
                .threeDSServerTransID(sessionState.getTransactionId())
                .pan(cardData.getPan())
                .cardholderName(cardData.getCardholderName())
                .expDate(cardData.getExpYear() + " " + cardData.getExpMonth())
                .notificationUrl(URLEncoder.encode(
                        UrlUtils.getCallbackUrl(
                                mpi20Properties.getCallbackUrl(),
                                mpi20Properties.getAcsNotificationPath()),
                        StandardCharsets.UTF_8))
                .amount(String.valueOf(cost.getAmount()))
                .currency(cost.getCurrency().getSymbolicCode())
                .build();
    }
}
