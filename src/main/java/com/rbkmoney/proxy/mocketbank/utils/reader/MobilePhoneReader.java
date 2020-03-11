package com.rbkmoney.proxy.mocketbank.utils.reader;

import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhone;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
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
