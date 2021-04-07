package com.rbkmoney.proxy.mocketbank.service.mpi20.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionStatus {

    AUTHENTICATION_SUCCESSFUL("Y"),
    NOT_AUTHENTICATED("N"),
    AUTHENTICATION_COULD_NOT_BE_PERFORMED("U"),
    ATTEMPTS_PROCESSING_PERFORMED("A"),
    CHALLENGE_REQUIRED("C"),
    REJECTED("R");

    private final String code;
}
