package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.utils.extractor.MobilePhoneReader;
import com.rbkmoney.proxy.mocketbank.utils.mobilephone.MobilePhone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class MobilePhoneConfiguration {

    @Value("${fixture.mobilephone}")
    private Resource fixtureMobilePhone;

    @Bean
    public List<MobilePhone> mobilePhoneList(MobilePhoneReader reader) throws IOException {
        return reader.readList(fixtureMobilePhone.getInputStream());
    }

}
