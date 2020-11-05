package com.rbkmoney.proxy.mocketbank.utils.payout;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CardPayout {
    private final String pan;
    private final String action;
    private final String paymentSystem;
}
