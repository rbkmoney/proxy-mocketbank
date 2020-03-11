package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.client.storage.model.CardDataProxyModel;
import com.rbkmoney.damsel.cds.AuthData;
import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.cds.CardSecurityCode;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.BankCardPaymentSystem;
import com.rbkmoney.damsel.domain.BankCardTokenProvider;
import com.rbkmoney.damsel.domain.Currency;
import com.rbkmoney.damsel.p2p_adapter.*;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.RecurrentTokenContext;
import com.rbkmoney.java.damsel.utils.creators.CdsPackageCreators;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.decorator.P2pServerHandlerLog;
import com.rbkmoney.proxy.mocketbank.service.mpi.MpiApi;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.EnrollmentStatus;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@Slf4j
public abstract class P2PIntegrationTest {

    protected String SESSION_ID = "TEST_SESSION_ID";
    protected String OPERATION_ID = "TEST_OPERATION_ID";
    protected String PAYMENT_SESSION_ID = "TEST_PAYMENT_SESSION_ID";
    protected String RECEIVER_TOKEN = "receiver";
    protected String SENDER_TOKEN = "sender";

    @Autowired
    protected P2pServerHandlerLog handler;

    @Autowired
    protected List<Card> cardList;

    @MockBean
    protected CdsClientStorage cdsStorage;

    @MockBean
    protected MpiApi mpiApi;

    protected Context createContext(BankCard bankCard) {
        Context context = new Context();
        context.setOperation(createOperationInfo(bankCard));
        context.setSession(createSession());
        context.setOptions(createOptions());
        return context;
    }

    protected Map<String, String> createOptions() {
        return new HashMap<>();
    }

    protected Session createSession() {
        Session session = new Session();
        return session.setId(SESSION_ID);
    }

    protected OperationInfo createOperationInfo(BankCard bankCard) {
        OperationInfo operation = new OperationInfo();

        operation.setProcess(new ProcessOperationInfo()
                .setBody(prepareCash())
                .setReceiver(createPaymentResource(bankCard, RECEIVER_TOKEN))
                .setSender(createPaymentResource(bankCard, SENDER_TOKEN))
                .setId(OPERATION_ID)
        );

        return operation;
    }

    protected Cash prepareCash() {
        return new Cash()
                .setAmount(6000L + (long) (Math.random() * 1000 + 1))
                .setCurrency(new Currency("Rubles", "RUB", (short) 643, (short) 2));
    }

    protected PaymentResource createPaymentResource(BankCard bankCard, String token) {
        PaymentResource paymentResource = new PaymentResource();
        bankCard.setToken(token);
        paymentResource.setDisposable(
                createDisposablePaymentResource(
                        createClientInfo(
                                TestData.FINGERPRINT,
                                TestData.IP_ADDRESS), PAYMENT_SESSION_ID,
                        createPaymentTool(bankCard)
                )
        );
        return paymentResource;
    }

    protected BankCard createBankCard(String token) {
        return new BankCard().setTokenProvider(BankCardTokenProvider.applepay)
                .setToken(token)
                .setPaymentSystem(BankCardPaymentSystem.mastercard)
                .setBin(TestData.DEFAULT_BIN);
    }

    protected void mockCds(CardData cardData, BankCard bankCard) {
        CardDataProxyModel proxyModel = CardDataProxyModel.builder()
                .cardholderName(bankCard.getCardholderName())
                .expMonth(cardData.getExpDate().getMonth())
                .expYear(cardData.getExpDate().getYear())
                .pan(cardData.getPan())
                .build();

        Mockito.when(cdsStorage.getCardData(anyString())).thenReturn(cardData);
        Mockito.when(cdsStorage.getCardData((RecurrentTokenContext) any())).thenReturn(proxyModel);
        Mockito.when(cdsStorage.getCardData((PaymentContext) any())).thenReturn(proxyModel);
        Mockito.when(cdsStorage.getSessionData((RecurrentTokenContext) any())).thenReturn(CdsPackageCreators.createSessionData(AuthData.card_security_code(new CardSecurityCode(cardData.getCvv()))));
        Mockito.when(cdsStorage.getSessionData((PaymentContext) any())).thenReturn(CdsPackageCreators.createSessionData(AuthData.card_security_code(new CardSecurityCode(cardData.getCvv()))));
    }

    protected void mockMpiVerify(EnrollmentStatus mpiEnrollmentStatus) {
        VerifyEnrollmentResponse response = new VerifyEnrollmentResponse();
        response.setAcsUrl(TestData.DEFAULT_ACS_URL);
        response.setEnrolled(mpiEnrollmentStatus.getStatus());
        response.setPaReq(TestData.DEFAULT_PAREQ);
        Mockito.when(mpiApi.verifyEnrollment((CardDataProxyModel) any())).thenReturn(response);
    }

    protected void mockMpi(TransactionStatus mpiTransactionStatus) {
        ValidatePaResResponse paResResponse = new ValidatePaResResponse();
        paResResponse.setTransactionStatus(mpiTransactionStatus.getStatus());
        Mockito.when(mpiApi.validatePaRes(any(), any())).thenReturn(paResResponse);
    }

}
