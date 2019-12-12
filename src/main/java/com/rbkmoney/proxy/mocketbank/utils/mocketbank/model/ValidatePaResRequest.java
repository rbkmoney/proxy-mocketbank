package com.rbkmoney.proxy.mocketbank.utils.mocketbank.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ValidatePaResRequest {
    private String pan;
    private String paRes;
}
