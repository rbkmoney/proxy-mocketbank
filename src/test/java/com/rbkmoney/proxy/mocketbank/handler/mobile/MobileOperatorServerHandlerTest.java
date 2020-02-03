package com.rbkmoney.proxy.mocketbank.handler.mobile;

import com.rbkmoney.mnp.Operator;
import com.rbkmoney.mnp.PhoneNumber;
import com.rbkmoney.mnp.RequestParams;
import com.rbkmoney.mnp.ResponseData;
import com.rbkmoney.proxy.mocketbank.exception.MobileOperatorException;
import com.rbkmoney.proxy.mocketbank.handler.mobile.operator.MobileOperatorServerHandler;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MobileOperatorServerHandlerTest {

    @Autowired
    private MobileOperatorServerHandler handler;

    @Test
    public void lookupTest() throws TException {
        // Get operator by phone number
        RequestParams requestParams = createRequestParams("9151111111");
        ResponseData responseData = handler.lookup(requestParams);
        assertEquals(Operator.mts, responseData.getOperator());

        requestParams = createRequestParams("9001111111");
        responseData = handler.lookup(requestParams);
        assertEquals(Operator.beeline, responseData.getOperator());
    }

    private RequestParams createRequestParams(String phoneNumber) {
        RequestParams params = new RequestParams();
        params.setPhone(new PhoneNumber("7", phoneNumber));
        params.setOptions(Collections.emptyMap());
        return params;
    }

    @Test(expected = MobileOperatorException.class)
    public void lookupExceptionTest() throws TException {
        // Get exception by phone number if phone not found in lists
        RequestParams requestParams = createRequestParams("9999999999");
        handler.lookup(requestParams);
    }

}
