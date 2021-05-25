package com.rbkmoney.proxy.mocketbank.utils.qps;

import com.rbkmoney.damsel.proxy_provider.Cash;
import com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QpsCreatorsTest {

    @Test
    void createQpsParamsTest() {
        MultiValueMap params = QpsCreators.createQpsParams("invoiceId", prepareCash());
        String payload = UrlUtils.getCallbackUrl("http://127.0.0.1:8019", "qps", params);

        assertEquals(
                "http://127.0.0.1:8019/qps?cur=RUB&bank=100000000000&crc=AB75&sum=10000&id=invoiceId&type=02",
                payload);
    }

    private Cash prepareCash() {
        return DomainPackageCreators.createCash(10000L, "Rubles", 643, "RUB", 2);
    }

}