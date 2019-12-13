package com.rbkmoney.proxy.mocketbank.utils.mocketbank.model;

import org.springframework.util.MultiValueMap;

public interface PrepareFieldsObject {
    MultiValueMap<String, Object> prepareFields();
}
