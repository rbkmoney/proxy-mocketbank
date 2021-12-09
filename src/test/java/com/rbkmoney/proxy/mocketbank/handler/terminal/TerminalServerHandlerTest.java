package com.rbkmoney.proxy.mocketbank.handler.terminal;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.proxy_provider.*;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.handler.IntegrationTest;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createPaymentResourceDisposablePaymentResource;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TerminalServerHandlerTest extends IntegrationTest {

    @Autowired
    private TerminalServerHandler handler;

    @Test
    void testProcess() throws TException {
        PaymentTool paymentTool = PaymentTool.payment_terminal(new PaymentTerminal());
        DisposablePaymentResource disposablePaymentResource = createDisposablePaymentResource(
                createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                TestData.SESSION_ID,
                paymentTool
        );
        PaymentResource paymentResource = createPaymentResourceDisposablePaymentResource(disposablePaymentResource);

        PaymentContext paymentContext = getContext(paymentResource, createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Terminal processPayment isn`t success", isSuccess(proxyResult));

        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        paymentContext.getSession().setTarget(createTargetCaptured());

        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success", isSuccess(proxyResult));
    }

}
