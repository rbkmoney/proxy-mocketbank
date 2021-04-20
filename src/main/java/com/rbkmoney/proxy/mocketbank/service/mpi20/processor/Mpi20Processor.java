package com.rbkmoney.proxy.mocketbank.service.mpi20.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.damsel.user_interaction.UserInteraction;
import com.rbkmoney.proxy.mocketbank.service.mpi20.Mpi20Client;
import com.rbkmoney.proxy.mocketbank.service.mpi20.converter.*;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.Error;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;

@Component
@RequiredArgsConstructor
public class Mpi20Processor {

    private final Mpi20Client mpi20Client;
    private final CtxToPreparationConverter ctxToPreparationConverter;
    private final CtxToAuthConverter ctxToAuthConverter;
    private final CtxToResultConverter ctxToResultConverter;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public PaymentProxyResult processPrepare(PaymentContext context) {
        PreparationRequest request = ctxToPreparationConverter.convert(context);
        PreparationResponse response = mpi20Client.prepare(request);
        Intent intent = buildPrepareIntent(request, response);
        SessionState sessionState = null;
        if (intent.isSetSuspend()) {
            sessionState = new SessionState(response.getThreeDSServerTransID(), Mpi20State.PREPARE);
        }
        return createPaymentProxyResult(intent, objectMapper.writeValueAsBytes(sessionState));
    }

    @SneakyThrows
    public PaymentCallbackProxyResult processAuth(PaymentContext context) {
        AuthenticationRequest request = ctxToAuthConverter.convert(context);
        AuthenticationResponse response = mpi20Client.auth(request);
        Intent intent = buildAuthIntent(request, response);
        SessionState sessionState = null;
        if (intent.isSetSuspend()) {
            sessionState = new SessionState(response.getThreeDSServerTransID(), Mpi20State.AUTH);
        }
        return createCallbackProxyResult(intent, objectMapper.writeValueAsBytes(sessionState), null);
    }

    @SneakyThrows
    public PaymentCallbackProxyResult processResult(PaymentContext context) {
        ResultRequest request = ctxToResultConverter.convert(context);
        ResultResponse response = mpi20Client.result(request);
        Intent intent = buildResultIntent(request, response);
        SessionState sessionState = null;
        if (intent.getFinish().getStatus().isSetSuccess()) {
            sessionState = new SessionState(response.getThreeDSServerTransID(), Mpi20State.RESULT);
        }
        return createCallbackProxyResult(intent, objectMapper.writeValueAsBytes(sessionState), null);
    }

    private Intent buildPrepareIntent(PreparationRequest request, PreparationResponse response) {
        if (isPreparationSuccess(response)) {
            String tag = response.getThreeDSServerTransID();
            Map<String, String> params = Map.of(
                    "threeDSMethodData", response.getThreeDSMethodData(),
                    "termUrl", request.getNotificationUrl());
            UserInteraction interaction = createPostUserInteraction(response.getThreeDSMethodURL(), params);
            return createIntentWithSuspendIntent(tag, 10, interaction);
        } else {
            return createFinishIntentSuccess();
        }
    }

    private Intent buildAuthIntent(AuthenticationRequest request, AuthenticationResponse response) {
        if (isAuthSuccess(response)) {
            String tag = response.getThreeDSServerTransID();
            Map<String, String> params = Map.of(
                    "creq", response.getCreq(),
                    "termUrl", request.getNotificationUrl());
            UserInteraction interaction = createPostUserInteraction(response.getAcsUrl(), params);
            return createIntentWithSuspendIntent(tag, 10, interaction);
        } else {
            return createFinishIntentFailure(response.getError().getCode(), response.getError().getTitle());
        }
    }

    private Intent buildResultIntent(ResultRequest request, ResultResponse response) {
        if (isResultSuccess(response)) {
            return createFinishIntentSuccess();
        } else {
            return createFinishIntentFailure(response.getError().getCode(), response.getError().getTitle());
        }
    }

    private boolean isPreparationSuccess(PreparationResponse response) {
        return isResponseHasNoError(response.getError())
                && "2".equals(response.getProtocolVersion());
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
