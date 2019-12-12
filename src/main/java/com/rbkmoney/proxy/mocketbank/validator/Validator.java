package com.rbkmoney.proxy.mocketbank.validator;

import java.util.Map;

public interface Validator<O> {
    void validate(O object, Map<String, String> options);
}
