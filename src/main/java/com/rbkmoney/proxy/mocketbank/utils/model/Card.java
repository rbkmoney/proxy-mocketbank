package com.rbkmoney.proxy.mocketbank.utils.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Card {
    private final String pan;
    private final String action;
    private final String paymentSystem;
}
