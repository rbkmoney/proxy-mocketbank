package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.domain.Failure;
import com.rbkmoney.damsel.p2p_adapter.CallbackResult;
import com.rbkmoney.damsel.p2p_adapter.ProcessResult;
import com.rbkmoney.damsel.proxy_provider.PaymentCallbackResult;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenCallbackResult;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenProxyResult;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.damsel.utils.creators.P2pAdapterCreators;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorBuilder {

    public static ProcessResult prepareP2pError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return P2pAdapterCreators.createP2pResultFailure(failure);
    }

    public static ProcessResult prepareP2pError(ErrorMapping errorMapping, CardAction action) {
        return prepareP2pError(errorMapping, action.getAction());
    }

    public static ProcessResult prepareP2pError(ErrorMapping errorMapping, String code) {
        return prepareP2pError(errorMapping, code, code);
    }

    public static PaymentProxyResult prepareError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createProxyResultFailure(failure);
    }

    public static PaymentProxyResult prepareError(ErrorMapping errorMapping, CardAction action) {
        return prepareError(errorMapping, action.getAction());
    }

    public static PaymentProxyResult prepareError(ErrorMapping errorMapping, String code) {
        return prepareError(errorMapping, code, code);
    }

    public static RecurrentTokenProxyResult prepareRecurrentTokenError(ErrorMapping errorMapping, String code) {
        return prepareRecurrentTokenError(errorMapping, code, code);
    }

    public static RecurrentTokenProxyResult prepareRecurrentTokenError(ErrorMapping errorMapping, CardAction action) {
        return prepareRecurrentTokenError(errorMapping, action.getAction());
    }

    public static RecurrentTokenProxyResult prepareRecurrentTokenError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createRecurrentTokenProxyResultFailure(failure);
    }

    public static CallbackResult prepareP2pCallbackError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return P2pAdapterCreators.createP2pCallbackResultFailure(failure);
    }

    public static CallbackResult prepareP2pCallbackResultFailure(ErrorMapping errorMapping, String code, CardAction action) {
        return prepareP2pCallbackError(errorMapping, code, action.getAction());
    }

    public static PaymentCallbackResult prepareCallbackError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createCallbackResultFailure(failure);
    }

    public static PaymentCallbackResult prepareCallbackError(ErrorMapping errorMapping, String code, CardAction action) {
        return prepareCallbackError(errorMapping, code, action.getAction());
    }

    public static RecurrentTokenCallbackResult prepareRecurrentCallbackError(ErrorMapping errorMapping, String code, CardAction action) {
        return prepareRecurrentCallbackError(errorMapping, code, action.getAction());
    }

    public static RecurrentTokenCallbackResult prepareRecurrentCallbackError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createRecurrentTokenCallbackResultFailure(failure);
    }

}
