package com.rbkmoney.proxy.mocketbank.utils.mocketbank.utils;

import com.rbkmoney.woody.api.flow.error.WErrorDefinition;
import com.rbkmoney.woody.api.flow.error.WErrorType;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.api.trace.context.TraceContext;
import com.rbkmoney.woody.thrift.impl.http.error.THTransportErrorMapper;

public class MocketBankUtils {

    /**
     * Kludge
     */
    public static boolean isUndefinedResultOrUnavailable(Exception exception) {
        WErrorDefinition definition;
        if(exception instanceof WRuntimeException) {
            definition = ((WRuntimeException) exception).getErrorDefinition();
        } else {
            THTransportErrorMapper errorMapper = new THTransportErrorMapper();
            definition = errorMapper.mapToDef(exception, TraceContext.getCurrentTraceData().getActiveSpan());
        }

        boolean undefined = (definition != null && WErrorType.UNDEFINED_RESULT.getKey().equals(definition.getErrorType().getKey()));
        boolean unavailable = (definition != null && WErrorType.UNAVAILABLE_RESULT.getKey().equals(definition.getErrorType().getKey()));

        return (undefined || unavailable);
    }

}
