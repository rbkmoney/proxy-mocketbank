package com.rbkmoney.proxy.mocketbank.utils.extractor;

import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhone;

import java.io.InputStream;
import java.util.List;

public class MobilePhoneReader implements BeanReader<MobilePhone> {

    private static final String REGEXP = ", ";

    @Override
    public List<MobilePhone> readList(InputStream is) {
        return extractListFromFile(is,
                line -> {
                    String[] p = line.split(REGEXP);
                    return new MobilePhone(p[0], p[1], p[2]);
                });
    }
}
