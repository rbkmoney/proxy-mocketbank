package com.rbkmoney.proxy.mocketbank.service.mpi20.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.user_interaction.UserInteraction;
import com.rbkmoney.proxy.mocketbank.configuration.properties.TimerProperties;
import com.rbkmoney.proxy.mocketbank.service.mpi20.Mpi20Client;
import com.rbkmoney.proxy.mocketbank.service.mpi20.converter.*;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.Error;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.*;
import com.rbkmoney.proxy.mocketbank.utils.CreatorUtils;
import com.rbkmoney.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.extractors.OptionsExtractors.extractRedirectTimeout;
import static com.rbkmoney.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Mpi20Processor {

    private final Mpi20Client mpi20Client;
    private final CtxToPreparationConverter ctxToPreparationConverter;
    private final CtxToAuthConverter ctxToAuthConverter;
    private final CtxToResultConverter ctxToResultConverter;
    private final ObjectMapper objectMapper;
    private final TimerProperties timerProperties;

    @SneakyThrows
    public PaymentProxyResult processPrepare(PaymentContext context) {
        PreparationRequest request = ctxToPreparationConverter.convert(context);
        PreparationResponse response = mpi20Client.prepare(request);
        Intent intent = buildPrepareIntent(
                request,
                response,
                extractRedirectTimeout(context.getOptions(), timerProperties.getRedirectTimeout()));
        SessionState sessionState = null;
        if (intent.isSetSuspend()) {
            sessionState = new SessionState(
                    response.getThreeDSServerTransID(),
                    Mpi20State.PREPARE,
                    request.getNotificationUrl(),
                    new HashMap<>());
        }
        return createPaymentProxyResult(intent, objectMapper.writeValueAsBytes(sessionState));
    }

    @SneakyThrows
    public PaymentCallbackProxyResult processAuth(PaymentContext context) {
        AuthenticationRequest request = ctxToAuthConverter.convert(context);
        AuthenticationResponse response = mpi20Client.auth(request);
        Intent intent = buildAuthIntent(
                response,
                extractRedirectTimeout(context.getOptions(), timerProperties.getRedirectTimeout()));
        SessionState sessionState = null;
        Map<String, String> params = Map.of(
                CREQ, response.getCreq(),
                TERM_URL, request.getNotificationUrl());
        if (intent.isSetSuspend()) {
            sessionState = new SessionState(
                    response.getThreeDSServerTransID(),
                    Mpi20State.AUTH,
                    request.getNotificationUrl(),
                    params);
        }
        return createCallbackProxyResult(intent, objectMapper.writeValueAsBytes(sessionState),
                CreatorUtils.createDefaultTransactionInfo(context));
    }

    @SneakyThrows
    public PaymentCallbackProxyResult processResult(PaymentContext context) {
        ResultRequest request = ctxToResultConverter.convert(context);
        ResultResponse response = mpi20Client.result(request);
        Intent intent = buildResultIntent(response);
        SessionState sessionState = null;
        if (intent.getFinish().getStatus().isSetSuccess()) {
            sessionState = new SessionState(
                    response.getThreeDSServerTransID(),
                    Mpi20State.RESULT,
                    null,
                    new HashMap<>());
        }
        return createCallbackProxyResult(intent, objectMapper.writeValueAsBytes(sessionState),
                CreatorUtils.createDefaultTransactionInfo(context));
    }

    private Intent buildPrepareIntent(PreparationRequest request,
                                      PreparationResponse response,
                                      int timerRedirectTimeout) {
        if (isPreparationSuccess(response)) {
            String tag = SuspendPrefix.PAYMENT.getPrefix() + response.getThreeDSServerTransID();
            Map<String, String> params = Map.of(
                    THREE_DS_METHOD_DATA, response.getThreeDSMethodData(),
                    TERM_URL, request.getNotificationUrl());
            UserInteraction interaction = createPostUserInteraction(response.getThreeDSMethodURL(), params);
            return createIntentWithSuspendIntent(tag, timerRedirectTimeout, interaction);
        } else {
            return createFinishIntentSuccess();
        }
    }

    private Intent buildAuthIntent(AuthenticationResponse response,
                                   int timerRedirectTimeout) {
        if (isAuthSuccess(response)) {
            String tag = SuspendPrefix.PAYMENT.getPrefix() + response.getThreeDSServerTransID();
            return createIntentWithSuspendIntent(tag, timerRedirectTimeout);
        } else {
            return createFinishIntentFailure(response.getError().getCode(), response.getError().getTitle());
        }
    }

    private Intent buildResultIntent(ResultResponse response) {
        if (isResultSuccess(response)) {
            return createFinishIntentSuccess();
        } else {
            return createFinishIntentFailure(response.getError().getCode(), response.getError().getTitle());
        }
    }

    private boolean isPreparationSuccess(PreparationResponse response) {
        return isResponseHasNoError(response.getError())
                && PROTOCOL_VERSION_2.equals(response.getProtocolVersion());
    }

    private boolean isAuthSuccess(AuthenticationResponse response) {
        return isResponseHasNoError(response.getError())
                && response.getTransStatus().equals(TransactionStatus.CHALLENGE_REQUIRED.getCode());
    }

    private boolean isResultSuccess(ResultResponse response) {
        return isResponseHasNoError(response.getError())
                && response.getTransStatus().equals(TransactionStatus.AUTHENTICATION_SUCCESSFUL.getCode());
    }

    private boolean isResponseHasNoError(Error error) {
        return error.getCode() == null && error.getTitle() == null;
    }
}
