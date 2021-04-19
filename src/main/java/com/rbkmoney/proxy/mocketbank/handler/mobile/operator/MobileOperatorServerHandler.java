package com.rbkmoney.proxy.mocketbank.handler.mobile.operator;

import com.rbkmoney.mnp.*;
import com.rbkmoney.proxy.mocketbank.exception.MobileOperatorException;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhone;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhoneUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MobileOperatorServerHandler implements MnpSrv.Iface {

    private final List<MobilePhone> mobilePhones;

    @Override
    public ResponseData lookup(RequestParams requestParams) {
        String phoneNumber = MobilePhoneUtils.preparePhoneNumber(requestParams.getPhone());
        Optional<MobilePhone> mobilePhone = MobilePhoneUtils.extractPhoneByNumber(mobilePhones, phoneNumber);
        if (!mobilePhone.isPresent()) {
            throw new MobileOperatorException(
                    String.format("Phone number %s not correct or not supported", phoneNumber));
        }
        return new ResponseData(Operator.valueOf(mobilePhone.get().getOperator()));
    }

}
