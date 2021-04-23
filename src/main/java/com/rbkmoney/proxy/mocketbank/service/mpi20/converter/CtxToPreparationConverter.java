package com.rbkmoney.proxy.mocketbank.service.mpi20.converter;

import com.rbkmoney.cds.client.storage.CdsClientStorage;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.proxy.mocketbank.configuration.properties.Mpi20Properties;
import com.rbkmoney.proxy.mocketbank.service.mpi20.model.PreparationRequest;
import com.rbkmoney.proxy.mocketbank.utils.UrlUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CtxToPreparationConverter implements Converter<PaymentContext, PreparationRequest> {

    private final CdsClientStorage cds;
    private final Mpi20Properties mpi20Properties;

    @Override
    @SneakyThrows
    public PreparationRequest convert(PaymentContext context) {

        CardDataProxyModel cardData = cds.getCardData(context);

        return PreparationRequest.builder()
                .pan(cardData.getPan())
                .notificationUrl(UrlUtils.getCallbackUrl(mpi20Properties.getCallbackUrl(),
                        mpi20Properties.getThreeDsMethodNotificationPath()))
                .build();
    }
}
