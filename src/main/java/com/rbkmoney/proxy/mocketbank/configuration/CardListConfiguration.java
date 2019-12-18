package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.utils.extractor.CardReader;
import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@Configuration
public class CardListConfiguration {

    @Value("${fixture.cards}")
    private Resource fixtureCards;

    @Bean
    public List<Card> cardList() throws IOException {
        return new CardReader().readList(fixtureCards.getInputStream());
    }

}
