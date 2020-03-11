package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rbkmoney.java.damsel.utils.extractors.WithdrawalsProviderAdapterPackageExtractors.isSuccess;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.identity-document-storage.url=http://127.0.0.1:8021/v1/identity_document_storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerTest extends OctIntegrationTest {

    @Test
    public void testProcessWithdrawal() throws TException {
        ProcessResult result = handler.processWithdrawal(
                createWithdrawal(),
                Value.str(""),
                createProxyOptions()
        );
        log.info("Response processWithdrawal {}", result);
        assertTrue("Result processWithdrawal isn`t success", isSuccess(result));
    }

}
