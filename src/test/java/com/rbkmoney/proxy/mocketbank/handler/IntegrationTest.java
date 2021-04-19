package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.Cash;
import com.rbkmoney.damsel.proxy_provider.InvoicePaymentRefund;
import com.rbkmoney.damsel.proxy_provider.Shop;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.java.cds.utils.creators.CdsPackageCreators;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.decorator.PaymentServerHandlerMdcLog;
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

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createDisposablePaymentResource;
import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createInvoice;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@Slf4j
public abstract class IntegrationTest {

    protected String invoiceId = "TEST_INVOICE";
    protected String paymentId = "TEST_PAYMENT";
    protected String recurrentId = "TEST_RECURRENT";
    protected String refundId = "TEST_REFUND";

    @Autowired
    protected PaymentServerHandlerMdcLog handler;

    @Autowired
    protected List<Card> cardList;

    @MockBean
    protected CdsClientStorage cdsStorage;

    @MockBean
    protected MpiApi mpiApi;

    protected Map<String, String> prepareProxyOptions() {
        return new HashMap<>();
    }

    protected Shop prepareShop() {
        ShopLocation shopLocation = new ShopLocation();
        shopLocation.setUrl("url");
        return new Shop()
                .setId("shop_id")
                .setCategory(new Category().setName("CategoryName").setDescription("Category description"))
                .setDetails(new ShopDetails().setName("ShopName").setDescription("Shop description"))
                .setLocation(shopLocation);
    }

    protected Cash prepareCash() {
        return DomainPackageCreators.createCash(
                10000L, "Rubles", 643, "RUB", 2);
    }

    protected PaymentInfo getPaymentInfo(String sessionId, BankCard bankCard, TransactionInfo transactionInfo) {
        PaymentResource paymentResource = getPaymentResource(sessionId, bankCard);
        PaymentInfo paymentInfo = getPaymentInfo(transactionInfo, paymentResource);
        paymentInfo.setCapture(prepareInvoicePaymentCapture());
        paymentInfo.setRefund(createInvoicePaymentRefund(transactionInfo));
        return paymentInfo;
    }

    protected PaymentInfo getPaymentInfo(TransactionInfo transactionInfo, PaymentResource paymentResource) {
        PaymentInfo paymentInfo = createPaymentInfo(
                createInvoice(
                        invoiceId,
                        TestData.CREATED_AT,
                        prepareCash()
                ),
                prepareShop(),
                createInvoicePaymentWithTrX(
                        paymentId,
                        TestData.CREATED_AT,
                        paymentResource,
                        prepareCash(),
                        transactionInfo
                ).setMakeRecurrent(Boolean.FALSE));

        paymentInfo.setCapture(prepareInvoicePaymentCapture());
        paymentInfo.setRefund(createInvoicePaymentRefund(transactionInfo));
        return paymentInfo;
    }

    private InvoicePaymentRefund createInvoicePaymentRefund(TransactionInfo transactionInfo) {
        InvoicePaymentRefund invoicePaymentRefund = new InvoicePaymentRefund();
        invoicePaymentRefund.setId(refundId);
        invoicePaymentRefund.setTrx(transactionInfo);
        return invoicePaymentRefund;
    }

    private InvoicePaymentCapture prepareInvoicePaymentCapture() {
        InvoicePaymentCapture invoicePaymentCapture = new InvoicePaymentCapture();
        invoicePaymentCapture.setCost(prepareCash());
        return invoicePaymentCapture;
    }

    protected PaymentResource getPaymentResource(String sessionId, BankCard bankCard) {
        return createPaymentResourceDisposablePaymentResource(
                createDisposablePaymentResource(
                        createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                        sessionId,
                        createPaymentTool(bankCard)
                )
        );
    }

    protected PaymentContext getContext(
            BankCard bankCard,
            TargetInvoicePaymentStatus target,
            TransactionInfo transactionInfo
    ) {
        byte[] state = new byte[0];
        return createContext(
                getPaymentInfo(TestData.SESSION_ID, bankCard, transactionInfo),
                createSession(target, state),
                prepareProxyOptions()
        );
    }

    protected PaymentContext getContext(
            PaymentResource paymentResource,
            TargetInvoicePaymentStatus target,
            TransactionInfo transactionInfo
    ) {
        byte[] state = new byte[0];
        return createContext(
                getPaymentInfo(transactionInfo, paymentResource),
                createSession(target, state),
                prepareProxyOptions()
        );
    }

    protected RecurrentTokenContext createRecurrentTokenContext(BankCard bankCard) {
        RecurrentTokenContext context = new RecurrentTokenContext();
        context.setSession(new RecurrentTokenSession());
        context.setTokenInfo(
                createRecurrentTokenInfo(
                        createRecurrentPaymentTool(
                                createDisposablePaymentResource(
                                        createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                                        TestData.SESSION_ID,
                                        createPaymentTool(bankCard)
                                )
                        ).setId(recurrentId)
                )
        );
        return context;
    }

    protected PaymentResource getPaymentResourceRecurrent(String token) {
        return createPaymentResourceRecurrentPaymentResource(
                createRecurrentPaymentResource(token)
        );
    }

    protected void mockCds(CardData cardData, BankCard bankCard) {
        CardDataProxyModel proxyModel = CardDataProxyModel.builder()
                .cardholderName(bankCard.getCardholderName())
                .expMonth(bankCard.getExpDate().getMonth())
                .expYear(bankCard.getExpDate().getYear())
                .pan(cardData.getPan())
                .build();

        Mockito.when(cdsStorage.getCardData(anyString())).thenReturn(cardData);
        Mockito.when(cdsStorage.getCardData((RecurrentTokenContext) any())).thenReturn(proxyModel);
        Mockito.when(cdsStorage.getCardData((PaymentContext) any())).thenReturn(proxyModel);
        Mockito.when(cdsStorage.getSessionData((RecurrentTokenContext) any()))
                .thenReturn(CdsPackageCreators.createSessionDataWithCvv(TestData.DEFAULT_CVV));
        Mockito.when(cdsStorage.getSessionData((PaymentContext) any()))
                .thenReturn(CdsPackageCreators.createSessionDataWithCvv(TestData.DEFAULT_CVV));
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
