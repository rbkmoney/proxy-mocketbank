package com.rbkmoney.proxy.mocketbank.configuration;

import com.rbkmoney.proxy.mocketbank.utils.model.Card;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class CardListConfiguration {

    @Value("${fixture.cards}")
    private Resource fixtureCards;

    @Bean
    public List<Card> cardList() throws IOException {
        return extractCardListFromFile(fixtureCards.getInputStream());
    }

    private static List<Card> extractCardListFromFile(InputStream is) {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        return br.lines().skip(1).map(mapToCard).collect(Collectors.toList());
    }

    private static Function<String, Card> mapToCard = (line) -> {
        String[] p = line.split(", ");
        return new Card(p[0], p[1], p[2]);
    };

}
