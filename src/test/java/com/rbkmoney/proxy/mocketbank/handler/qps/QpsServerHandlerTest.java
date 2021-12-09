package com.rbkmoney.proxy.mocketbank.handler.qps;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.handler.IntegrationTest;
import com.rbkmoney.proxy.mocketbank.handler.terminal.TerminalServerHandler;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createPaymentResourceDisposablePaymentResource;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSleep;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class QpsServerHandlerTest extends IntegrationTest {

    @Autowired
    private TerminalServerHandler handler;

    @Test
    void testProcess() throws TException {
        PaymentTerminal paymentTerminal = new PaymentTerminal();
        paymentTerminal.setTerminalTypeDeprecated(LegacyTerminalPaymentProvider.qps);
        PaymentTool paymentTool = PaymentTool.payment_terminal(paymentTerminal);
        DisposablePaymentResource disposablePaymentResource = createDisposablePaymentResource(
                createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                TestData.SESSION_ID,
                paymentTool
        );
        PaymentResource paymentResource = createPaymentResourceDisposablePaymentResource(disposablePaymentResource);

        PaymentContext paymentContext = getContext(paymentResource, createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Qps Terminal processPayment isn`t sleep", isSleep(proxyResult));

        paymentContext.getSession().setState(proxyResult.getNextState());
        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Qps Terminal processPayment isn`t sleep", isSuccess(proxyResult));

        paymentContext.getSession().setTarget(createTargetCaptured());
        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success", isSuccess(proxyResult));
    }

}
