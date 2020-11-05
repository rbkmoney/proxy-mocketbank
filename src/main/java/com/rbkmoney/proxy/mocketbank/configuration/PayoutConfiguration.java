package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayout;
import com.rbkmoney.proxy.mocketbank.utils.reader.CardPayoutReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class PayoutConfiguration {

    @Value("${fixture.payout}")
    private Resource fixturePayout;

    @Bean
    public List<CardPayout> cardPayoutList(CardPayoutReader reader) throws IOException {
        return reader.readList(fixturePayout.getInputStream());
    }

}
