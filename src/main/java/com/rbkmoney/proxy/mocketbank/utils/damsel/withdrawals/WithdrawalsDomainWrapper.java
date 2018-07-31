package com.rbkmoney.proxy.mocketbank.utils.damsel.withdrawals;

import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.proxy.mocketbank.utils.damsel.DomainWrapper;

import java.util.Collections;
import java.util.Map;

public class WithdrawalsDomainWrapper {

    // TransactionInfo
    public static TransactionInfo makeTransactionInfo(String paymentId, Map<String, String> extra, String timestamp) {
        return DomainWrapper.makeTransactionInfo(paymentId, extra, timestamp);
    }

    public static TransactionInfo makeTransactionInfo(String paymentId, Map<String, String> extra) {
        return DomainWrapper.makeTransactionInfo(paymentId, extra, null);
    }

    public static TransactionInfo makeTransactionInfo(String paymentId) {
        return DomainWrapper.makeTransactionInfo(paymentId, Collections.emptyMap(), null);
    }


}
