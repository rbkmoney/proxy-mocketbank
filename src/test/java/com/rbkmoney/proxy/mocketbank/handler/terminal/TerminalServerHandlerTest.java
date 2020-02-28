package com.rbkmoney.proxy.mocketbank.handler.terminal;

import com.rbkmoney.damsel.domain.DisposablePaymentResource;
import com.rbkmoney.damsel.domain.PaymentTerminal;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.handler.IntegrationTest;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.*;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createPaymentResourceDisposablePaymentResource;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TerminalServerHandlerTest extends IntegrationTest {

    @Autowired
    private TerminalServerHandler handler;

    @Test
    public void testProcess() throws TException {
        PaymentTool paymentTool = PaymentTool.payment_terminal(new PaymentTerminal());
        DisposablePaymentResource disposablePaymentResource = createDisposablePaymentResource(
                createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                TestData.SESSION_ID,
                paymentTool
        );
        PaymentResource paymentResource = createPaymentResourceDisposablePaymentResource(disposablePaymentResource);

        PaymentContext paymentContext = getContext(paymentResource, createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Terminal processPayment is`n success", isSuccess(proxyResult));

        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        paymentContext.getSession().setTarget(createTargetCaptured());

        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process Capture is`n success", isSuccess(proxyResult));
    }

}