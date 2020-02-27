package com.rbkmoney.proxy.mocketbank.utils.p2p.creator;

import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.BankCardPaymentSystem;
import com.rbkmoney.damsel.domain.BankCardTokenProvider;
import com.rbkmoney.damsel.domain.Currency;
import com.rbkmoney.damsel.p2p_adapter.*;
import com.rbkmoney.proxy.mocketbank.utils.p2p.constant.P2pConstant;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;

@NoArgsConstructor
public class P2pCreator {

    public static Context createContext() {
        Context context = new Context();
        context.setOperation(createOperationInfo());
        context.setSession(createSession());
        context.setOptions(createOptions());
        return context;
    }

    public static Callback createCallback() {
        Callback callback = new Callback();
        callback.setTag(P2pConstant.CALLBACK_TAG);
        callback.setPayload(new byte[0]);
        return callback;
    }

    public static Map<String, String> createOptions() {
        return new HashMap<>();
    }

    public static Session createSession() {
        return new Session();
    }

    public static OperationInfo createOperationInfo() {
        OperationInfo operation = new OperationInfo();

        operation.setProcess(new ProcessOperationInfo()
                .setBody(prepareCash())
                .setReceiver(createPaymentResource(P2pConstant.RECEIVER_TOKEN))
                .setSender(createPaymentResource(P2pConstant.SENDER_TOKEN)));

        return operation;
    }

    public static Cash prepareCash() {
        return new Cash()
                .setAmount(6000L + (long) (Math.random() * 1000 + 1))
                .setCurrency(new Currency("Rubles", "RUB", (short) 643, (short) 2));
    }

    public static PaymentResource createPaymentResource(String token) {
        PaymentResource paymentResource = new PaymentResource();
        paymentResource.setDisposable(
                createDisposablePaymentResource(
                        createClientInfo(
                                P2pConstant.FINGERPRINT,
                                P2pConstant.IP_ADDRESS), token,
                        createPaymentTool(createBankCardMobile(token))
                )
        );
        return paymentResource;
    }

    public static BankCard createBankCardMobile(String token) {
        return new BankCard().setTokenProvider(BankCardTokenProvider.applepay)
                .setToken(token)
                .setPaymentSystem(BankCardPaymentSystem.mastercard)
                .setBin("1234");
    }

}
